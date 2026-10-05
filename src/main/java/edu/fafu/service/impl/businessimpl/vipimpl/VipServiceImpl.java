package edu.fafu.service.impl.businessimpl.vipimpl;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import edu.fafu.config.VipConfigCache;
import edu.fafu.database.entity.Result;
import edu.fafu.database.entity.User;
import edu.fafu.database.entity.VipConfig;
import edu.fafu.database.entity.VipTrade;
import edu.fafu.database.dto.response.common.ImageResponse;
import edu.fafu.database.dto.request.vip.VipBuyRequest;
import edu.fafu.database.dto.response.vip.VipLevelResponse;
import edu.fafu.database.dto.response.user.UserGoods;
import edu.fafu.database.mapper.businessmapper.vipmapper.VipGoodsMapper;
import edu.fafu.database.mapper.businessmapper.vipmapper.VipTradeMapper;
import edu.fafu.database.mapper.businessmapper.vipmapper.VipUserMapper;
import edu.fafu.exception.BusinessExceptionInterface;
import edu.fafu.service.service.businessservice.vipservice.VipService;
import edu.fafu.tool.image.ImageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class VipServiceImpl implements VipService, BusinessExceptionInterface {

    private static final String VIP_LEVEL_KEY_PREFIX = "vip:level:";
    private static final String CACHE_KEY_IMAGES = "vip:cache:images";
    private static final String CACHE_KEY_HOME_IMAGES = "vip:cache:homeImages";
    private static final String CACHE_KEY_IDS = "vip:cache:ids";
    private static final long CACHE_TTL_MINUTES = 1;

    @Autowired
    private VipConfigCache vipConfigCache;
    @Autowired
    private VipGoodsMapper vipGoodsMapper;
    @Autowired
    private VipUserMapper vipUserMapper;
    @Autowired
    private VipTradeMapper vipTradeMapper;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public Result<List<UserGoods>> vipGoods(Integer userId) {
        List<Integer> ids = loadVipIds();
        List<UserGoods> goodsList = vipGoodsMapper.selectByIds(ids, userId);
        return Result.success(goodsList);
    }

    @Override
    public Result<List<ImageResponse>> vipImage() {
        return Result.success(getOrLoadImages(CACHE_KEY_IMAGES, "vip/image"));
    }

    @Override
    public Result<List<ImageResponse>> vipHomeImage() {
        return Result.success(getOrLoadImages(CACHE_KEY_HOME_IMAGES, "vip/homeimage"));
    }

    @Override
    public Result<VipLevelResponse> vipLevel(Integer userId, Integer vipLevel) {
        if (vipLevel != null) {
            User user = new User();
            user.setId(userId);
            user.setLevel(vipLevel);
            return Result.success(new VipLevelResponse(user, vipConfigCache.getConfigMap()));
        }
        User user = new User();
        user.setId(userId);
        user = vipUserMapper.selectById(user);
        if (user == null) fail("用户不存在");
        int level = user.getLevel() != null ? user.getLevel() : 0;
        redisTemplate.opsForValue().set(VIP_LEVEL_KEY_PREFIX + userId, String.valueOf(level), 24, TimeUnit.HOURS);
        return Result.success(new VipLevelResponse(user, vipConfigCache.getConfigMap()));
    }

    @Override
    public Result<List<VipConfig>> vipConfig() {
        return Result.success(vipConfigCache.getAllConfigs());
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Result<String> buyVip(VipBuyRequest request, Integer userId) {
        ensureTrue(request.getLevel() != null && request.getLevel() >= 0, "无效的VIP等级");
        VipConfig targetConfig = vipConfigCache.getConfigMap().get(request.getLevel());
        ensureNotNull(targetConfig, "该等级VIP配置不存在");
        ensurePositive(request.getMoney(), "金额必须大于0");

        User user = new User();
        user.setId(userId);
        user = vipUserMapper.selectById(user);
        ensureNotNull(user, "用户不存在");

        ensureTrue(user.getLevel() == null || user.getLevel() < request.getLevel(), "当前等级已大于等于目标等级");

        ensureSufficientBalance(user.getBalance(), request.getMoney(), "余额不足");

        user.setBalance(user.getBalance().subtract(request.getMoney()));
        user.setLevel(request.getLevel());

        if (user.getVipCreateTime() != null && !isVipExpired(user)) {
            user.setVipDuration(user.getVipDuration() + targetConfig.getVipDuration());
        } else {
            user.setVipCreateTime(java.time.LocalDateTime.now());
            user.setVipDuration(targetConfig.getVipDuration() != null ? targetConfig.getVipDuration() : 30);
        }
        vipUserMapper.updateList(Collections.singletonList(user));

        VipTrade trade = new VipTrade();
        trade.setUserId(userId);
        trade.setMoney(request.getMoney());
        trade.setLevel(request.getLevel());
        trade.setNum(request.getNum() != null ? request.getNum() : 1);
        vipTradeMapper.insert(trade);

        redisTemplate.opsForValue().set(VIP_LEVEL_KEY_PREFIX + userId, request.getLevel().toString(), 24, TimeUnit.HOURS);

        return Result.success("购买成功");
    }

    private List<ImageResponse> loadImages(String dir) {
        List<ImageResponse> result = new ArrayList<>();
        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            String[] patterns = { "classpath:" + dir + "/*.webp", "classpath:" + dir + "/*.jpg" };
            for (String pattern : patterns) {
                org.springframework.core.io.Resource[] resources = resolver.getResources(pattern);
                for (org.springframework.core.io.Resource res : resources) {
                    String filename = res.getFilename();
                    if (filename == null || filename.isEmpty()) continue;
                    byte[] data = res.getInputStream().readAllBytes();
                    result.add(new ImageResponse(toRenderableDataUri(data), filename));
                }
            }
        } catch (Exception ignored) {
        }
        return result;
    }

    /**
     * 任意图片字节 → 小程序 <image> 可渲染的 PNG data URI。
     *
     * 微信 <image> 组件不解析 webp 的 base64 data URI（webp 属性只支持网络资源），
     * 之前直接输出 data:image/webp;base64 导致真机 Banner 全空白。
     * 这里用 ImageIO 解码后统一转 PNG 输出：
     * pom 已引入 TwelveMonkeys imageio-webp 插件提供 webp 解码能力；
     * ImageIO.read 按内容嗅探格式，不依赖文件名（3.webp 实为 JPEG 也能解码）。
     */
    private String toRenderableDataUri(byte[] data) {
        try {
            java.awt.image.BufferedImage image = javax.imageio.ImageIO.read(new java.io.ByteArrayInputStream(data));
            if (image != null) {
                java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
                javax.imageio.ImageIO.write(image, "png", out);
                return "data:image/png;base64," + Base64.getEncoder().encodeToString(out.toByteArray());
            }
        } catch (Exception ignored) {
        }
        // 解码失败兜底：按魔数标注真实类型，直接回原始字节（尽力而为）
        String type = "png";
        if (data.length > 12
                && "RIFF".equals(new String(data, 0, 4, StandardCharsets.US_ASCII))
                && "WEBP".equals(new String(data, 8, 4, StandardCharsets.US_ASCII))) {
            type = "webp";
        } else if (data.length > 2 && (data[0] & 0xFF) == 0xFF && (data[1] & 0xFF) == 0xD8) {
            type = "jpeg";
        }
        return "data:image/" + type + ";base64," + Base64.getEncoder().encodeToString(data);
    }

    private List<ImageResponse> getOrLoadImages(String cacheKey, String dir) {
        try {
            String cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                return objectMapper.readValue(cached, new TypeReference<List<ImageResponse>>() {});
            }
        } catch (Exception ignored) {}
        List<ImageResponse> images = loadImages(dir);
        try {
            redisTemplate.opsForValue().set(cacheKey, objectMapper.writeValueAsString(images), CACHE_TTL_MINUTES, TimeUnit.MINUTES);
        } catch (Exception ignored) {}
        return images;
    }

    private boolean isVipExpired(User user) {
        if (user.getVipCreateTime() == null || user.getVipDuration() == null) return true;
        java.time.LocalDateTime expireTime = user.getVipCreateTime().plusDays(user.getVipDuration());
        return java.time.LocalDateTime.now().isAfter(expireTime);
    }

    private List<Integer> loadVipIds() {
        try {
            String cached = redisTemplate.opsForValue().get(CACHE_KEY_IDS);
            if (cached != null) {
                return objectMapper.readValue(cached, new TypeReference<List<Integer>>() {});
            }
        } catch (Exception ignored) {}
        List<Integer> ids = new ArrayList<>();
        try {
            ClassPathResource resource = new ClassPathResource("vip/vip.txt");
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (!line.isEmpty()) {
                        ids.add(Integer.parseInt(line));
                    }
                }
            }
        } catch (Exception e) {
            return ids;
        }
        try {
            redisTemplate.opsForValue().set(CACHE_KEY_IDS, objectMapper.writeValueAsString(ids), CACHE_TTL_MINUTES, TimeUnit.MINUTES);
        } catch (Exception ignored) {}
        return ids;
    }
}