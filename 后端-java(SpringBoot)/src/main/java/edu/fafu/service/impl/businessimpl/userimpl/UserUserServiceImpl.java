package edu.fafu.service.impl.businessimpl.userimpl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.crypto.digest.DigestUtil;
import edu.fafu.tool.oss.OssUtil;
import edu.fafu.config.SystemConfig;
import edu.fafu.config.VipConfigCache;
import edu.fafu.database.entity.Background;
import edu.fafu.database.entity.Result;
import edu.fafu.database.entity.User;
import edu.fafu.database.entity.VipConfig;
import edu.fafu.database.dto.request.user.LoginRequest;
import edu.fafu.database.dto.request.user.RegisterRequest;
import edu.fafu.database.dto.request.user.WechatLoginRequest;
import edu.fafu.database.dto.request.user.UpdateRequest;
import edu.fafu.database.dto.response.user.UserInfoVO;
import edu.fafu.database.dto.response.user.WechatLoginResponse;
import edu.fafu.database.mapper.businessmapper.usermapper.BackgroundMapper;
import edu.fafu.database.mapper.businessmapper.usermapper.UserUserMapper;
import edu.fafu.database.mapper.businessmapper.vipmapper.VipConfigMapper;
import edu.fafu.exception.BusinessExceptionInterface;
import edu.fafu.service.service.businessservice.userservice.UserUserService;
import edu.fafu.service.service.toolservice.WechatService;
import edu.fafu.tool.captcha.CaptchaUtil;
import edu.fafu.tool.crypto.PathCryptoUtil;
import edu.fafu.tool.image.ImageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static edu.fafu.tool.captcha.CaptchaUtil.validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class UserUserServiceImpl implements UserUserService, CaptchaUtil, BusinessExceptionInterface {

    private static final Logger log = LoggerFactory.getLogger(UserUserServiceImpl.class);

    @Autowired
    private UserUserMapper userUserMapper;
    @Autowired
    private VipConfigCache vipConfigCache;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private BackgroundMapper backgroundMapper;
    @Autowired
    private VipConfigMapper vipConfigMapper;
    @Autowired
    private OssUtil ossUtil;
    @Autowired
    private SystemConfig systemConfig;
    @Autowired
    private WechatService wechatService;
    @Autowired
    private edu.fafu.tool.cache.QueryCache queryCache;

    @Override
    public Result<String> register(RegisterRequest request) {
        ensureEquals(request.getPassword(), request.getConfirmPassword(), "两次密码输入不一致");

        User checkName = new User();
        checkName.setUserName(request.getUserName());
        ensureEmpty(userUserMapper.selectList(checkName), "用户名已存在");

        User checkAccount = new User();
        checkAccount.setAccount(request.getAccount());
        ensureEmpty(userUserMapper.selectList(checkAccount), "账号已存在");

        if (!isBlank(request.getEmail())) {
            User checkEmail = new User();
            checkEmail.setEmail(request.getEmail());
            ensureEmpty(userUserMapper.selectList(checkEmail), "邮箱已被注册");
        }

        if (!isBlank(request.getPhone())) {
            User checkPhone = new User();
            checkPhone.setPhone(request.getPhone());
            ensureEmpty(userUserMapper.selectList(checkPhone), "手机号已被注册");
        }

        String salt = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        String hashedPassword = DigestUtil.sha256Hex(request.getPassword() + salt);

        User user = new User();
        user.setUserName(request.getUserName());
        user.setAccount(request.getAccount());
        user.setPassword(hashedPassword);
        user.setSalt(salt);
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setBalance(new BigDecimal("0"));
        user.setCreateTime(LocalDateTime.now());
        user.setDelete(false);
        user.setLevel(0);
        user.setUploadTime(LocalDateTime.now());
        user.setUpdateAvatar(0);
        user.setUploadBackground(0);
        user.setUploadGoods(0);

        userUserMapper.insert(user);
        return Result.success("注册成功");
    }

    @Override
    public Result<Map<String, String>> login(LoginRequest request) {
        validate(request.getCaptchaCode());

        String rateKey = "login:rate:limit:" + request.getAccount();
        String value = redisTemplate.opsForValue().get(rateKey);
        if (value != null && Integer.parseInt(value) >= systemConfig.getLoginMaxAttempts()) {
            long ttl = redisTemplate.getExpire(rateKey, TimeUnit.SECONDS);
            fail("登录尝试次数过多，还有" + ttl + "秒重试");
        }
        Long count = redisTemplate.opsForValue().increment(rateKey);
        if (count != null && count == 1) {
            redisTemplate.expire(rateKey, systemConfig.getLoginWindowSec(), TimeUnit.SECONDS);
        }

        User query = new User();
        query.setAccount(request.getAccount());
        User user = userUserMapper.selectById(query);
        ensureNotNull(user, "账号或密码错误");
        ensureFalse(Boolean.TRUE.equals(user.getDelete()), "账号已被封禁");

        String hashedPassword = DigestUtil.sha256Hex(request.getPassword() + user.getSalt());
        ensureEquals(hashedPassword, user.getPassword(), "账号或密码错误");

        StpUtil.login(user.getId());
        // 设置角色到Sa-Token Session
        String role = user.getRole() != null ? user.getRole() : User.ROLE_USER;
        StpUtil.getSession().set("role", role);
        String token = StpUtil.getTokenValue();

        Map<String, String> data = new HashMap<>();
        data.put("token", token);
        data.put("userId", PathCryptoUtil.encrypt(systemConfig, String.valueOf(user.getId())));
        return Result.success(data);
    }

    @Override
    public Result<WechatLoginResponse> wechatLogin(WechatLoginRequest request) {
        try {
            cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult session =
                    wechatService.code2Session(request.getCode());
            String openid = session.getOpenid();

            // 根据 openid 查找已有用户
            User query = new User();
            query.setWechatOpenid(openid);
            List<User> userList = userUserMapper.selectList(query);
            User user = userList.isEmpty() ? null : userList.get(0);

            boolean isNewUser = false;
            if (user == null) {
                // 新用户自动注册
                user = new User();
                user.setWechatOpenid(openid);
                user.setAccount("wx_" + openid.substring(0, 16));
                user.setUserName("微信用户" + openid.substring(0, 8));
                user.setPassword("");
                user.setSalt("");
                user.setPhone("");
                user.setEmail("");
                user.setDescribe("");
                user.setAvatarPath("");
                user.setRole(User.ROLE_USER);
                user.setBalance(new BigDecimal("0"));
                user.setCreateTime(LocalDateTime.now());
                user.setDelete(false);
                user.setLevel(0);
                user.setUploadTime(LocalDateTime.now());
                user.setUpdateAvatar(0);
                user.setUploadBackground(0);
                user.setUploadGoods(0);
                userUserMapper.insert(user);
                isNewUser = true;
            }

            StpUtil.login(user.getId());
            StpUtil.getSession().set("role", user.getRole() != null ? user.getRole() : User.ROLE_USER);
            String token = StpUtil.getTokenValue();

            WechatLoginResponse response = new WechatLoginResponse();
            response.setToken(token);
            response.setUserId(user.getId());
            response.setIsNewUser(isNewUser);
            return Result.success(response);
        } catch (Exception e) {
            log.error("微信登录失败: {}", e.getMessage());
            throw new RuntimeException("微信登录失败: " + e.getMessage());
        }
    }

    @Override
    public Result<String> update(UpdateRequest request, Integer userId) {
        User query = new User();
        query.setId(userId);
        User user = userUserMapper.selectById(query);
        ensureNotNull(user, "用户不存在");

        if (!isBlank(request.getUserName())) {
            if (!request.getUserName().equals(user.getUserName())) {
                User checkName = new User();
                checkName.setUserName(request.getUserName());
                ensureEmpty(userUserMapper.selectList(checkName), "用户名已存在");
            }
            user.setUserName(request.getUserName());
        }

        if (!isBlank(request.getEmail())) {
            if (!request.getEmail().equals(user.getEmail())) {
                User checkEmail = new User();
                checkEmail.setEmail(request.getEmail());
                ensureEmpty(userUserMapper.selectList(checkEmail), "邮箱已被注册");
            }
            user.setEmail(request.getEmail());
        }

        if (!isBlank(request.getPhone())) {
            if (!request.getPhone().equals(user.getPhone())) {
                User checkPhone = new User();
                checkPhone.setPhone(request.getPhone());
                ensureEmpty(userUserMapper.selectList(checkPhone), "手机号已被注册");
            }
            user.setPhone(request.getPhone());
        }

        if (!isBlank(request.getDescribe())) {
            user.setDescribe(request.getDescribe());
        }

        if (!isBlank(request.getPassword())) {
            ensureEquals(request.getPassword(), request.getConfirmPassword(), "两次密码输入不一致");
            String salt = user.getSalt() != null ? user.getSalt() : UUID.randomUUID().toString().replace("-", "").substring(0, 16);
            user.setSalt(salt);
            user.setPassword(DigestUtil.sha256Hex(request.getPassword() + salt));
        }

        userUserMapper.updateById(user);
        return Result.success("更新成功");
    }

    @Override
    public Result<UserInfoVO> getUserInfo(Integer userId) {
        UserInfoVO vo = queryCache.getOrLoad(
                "user:info:" + userId,
                UserInfoVO.class,
                () -> {
                    User query = new User();
                    query.setId(userId);
                    User user = userUserMapper.selectById(query);
                    ensureNotNull(user, "用户不存在");

                    Background bgQuery = new Background();
                    bgQuery.setUserId(userId);
                    List<Background> backgrounds = backgroundMapper.selectList(bgQuery);

                    UserInfoVO v = new UserInfoVO();
                    v.setId(user.getId());
                    v.setUserName(user.getUserName());
                    v.setAccount(user.getAccount());
                    v.setPhone(user.getPhone());
                    v.setEmail(user.getEmail());
                    v.setAvatarPath(user.getAvatarPath());
                    v.setDescribe(user.getDescribe());
                    v.setRole(user.getRole());
                    v.setLevel(user.getLevel());
                    v.setImagePath(user.getImagePath());

                    if (user.getLevel() != null && user.getLevel() > 0) {
                        VipConfig config = vipConfigMapper.selectById(user.getLevel());
                        if (config != null) {
                            v.setLevelName(config.getName());
                        }
                    }

                    if (user.getVipEndTime() != null) {
                        long remaining = ChronoUnit.MINUTES.between(LocalDateTime.now(), user.getVipEndTime());
                        v.setRemainingTime(Math.max(remaining, 0));
                    }

                    return v;
                },
                2
        );
        return Result.success(vo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> updateUserImage(MultipartFile file, Integer userId) {
        User query = new User();
        query.setId(userId);
        User user = userUserMapper.selectById(query);
        ensureNotNull(user, "用户不存在");

        // 限制上传频率
        ensureFalse(user.getUpdateAvatar() != null && user.getUpdateAvatar() >= vipConfigCache.getMaxAvatarUpdates(), "头像更新次数已达上限");

        // 保存头像
        String avatarPath = ImageUtil.saveAvatarToOss(systemConfig, ossUtil, file, userId);
        user.setAvatarPath(avatarPath);
        user.setUpdateAvatar(user.getUpdateAvatar() != null ? user.getUpdateAvatar() + 1 : 1);
        userUserMapper.updateById(user);
        return Result.success("头像更新成功");
    }

    @Override
    public Result<String> updateDescribe(String describe, Integer userId) {
        User query = new User();
        query.setId(userId);
        User user = userUserMapper.selectById(query);
        ensureNotNull(user, "用户不存在");
        user.setDescribe(describe);
        userUserMapper.updateById(user);
        return Result.success("更新描述成功");
    }

    @Override
    public Result<String> setDefaultAddress(Integer addressId, Integer userId) {
        User query = new User();
        query.setId(userId);
        User user = userUserMapper.selectById(query);
        ensureNotNull(user, "用户不存在");
        user.setDefaultAddressId(addressId);
        userUserMapper.updateById(user);
        return Result.success("默认地址设置成功");
    }

    @Override
    public Result<String> logout(Integer userId) {
        StpUtil.logout(userId);
        return Result.success("登出成功");
    }

    public Result<List<UserInfoVO.AddressItem>> getAddresses(Integer userId) {
        return null;
    }

    private boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }
}