package edu.fafu.service.impl.pageimpl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import edu.fafu.database.entity.Result;
import edu.fafu.database.entity.User;
import edu.fafu.database.dto.request.page.*;
import edu.fafu.database.dto.response.user.*;
import edu.fafu.database.mapper.businessmapper.usermapper.UserUserMapper;
import edu.fafu.database.mapper.pagemapper.UserPageMapper;
import edu.fafu.exception.BusinessExceptionInterface;
import edu.fafu.service.service.pageservice.UserPageService;
import edu.fafu.tool.cache.QueryCache;
import edu.fafu.tool.page.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tools.jackson.core.type.TypeReference;

import java.util.List;

@Service
public class UserPageServiceImpl implements UserPageService, BusinessExceptionInterface {

    private static final String CACHE_KEY_USER = "page:user:info:";
    private static final String CACHE_KEY_VIP_CONFIG = "page:user:vipConfig";
    private static final String CACHE_KEY_USER_BACKGROUND = "page:user:background:";
    private static final long CACHE_TTL_SHORT = 2;
    private static final long CACHE_TTL_MEDIUM = 5;

    @Autowired
    private UserPageMapper userPageMapper;
    @Autowired
    private UserUserMapper userUserMapper;
    @Autowired
    private QueryCache queryCache;

    @Override
    public Result<UserUser> pageUserUser(Integer userId) {
        UserUser user = queryCache.getOrLoad(
                CACHE_KEY_USER + userId,
                new TypeReference<UserUser>() {},
                () -> {
                    UserUser query = new UserUser();
                    query.setId(userId);
                    List<UserUser> list = userPageMapper.selectUserList(query);
                    if (list.isEmpty()) return null;
                    UserUser u = list.get(0);
                    UserBackground bgQuery = new UserBackground();
                    bgQuery.setUserId(userId);
                    Page<UserBackground> bgPage = new Page<>(1, -1);
                    List<UserBackground> bgList = userPageMapper.selectBackgroundList(bgPage, bgQuery).getRecords();
                    List<UserUser.BackgroundImage> backgrounds = bgList.stream().map(bg -> {
                        UserUser.BackgroundImage img = new UserUser.BackgroundImage();
                        img.setImagePath(bg.getImagePath());
                        img.setSequence(bg.getSequence());
                        return img;
                    }).toList();
                    u.setBackgrounds(backgrounds);
                    return u;
                },
                CACHE_TTL_SHORT
        );
        return Result.success(user);
    }

    @Override
    public Result<PageResult<UserAddress>> pageUserAddress(PageUserAddressRequest request, Integer userId) {
        User userQuery = new User();
        userQuery.setId(userId);
        User user = userUserMapper.selectById(userQuery);
        Integer defaultAddressId = user != null ? user.getDefaultAddressId() : null;

        Page<UserAddress> page = new Page<>(request.getPage(), request.getSize());
        page.addOrder(Boolean.TRUE.equals(request.getDesc()) ? OrderItem.desc("a.id") : OrderItem.asc("a.id"));
        UserAddress query = new UserAddress();
        query.setUserId(userId);
        IPage<UserAddress> result = userPageMapper.selectAddressList(page, query);
        for (UserAddress addr : result.getRecords()) {
            addr.setIsDefault(defaultAddressId != null && defaultAddressId.equals(addr.getId()));
        }
        return Result.success(new PageResult<>(result).addExtra("defaultAddressId", defaultAddressId));
    }

    @Override
    public Result<PageResult<UserCart>> pageUserCart(PageUserCartRequest request, Integer userId) {
        int cartCount = userPageMapper.selectCartCount(userId);
        Page<UserCart> page = new Page<>(request.getPage(), request.getSize());
        page.addOrder(Boolean.TRUE.equals(request.getDesc()) ? OrderItem.desc("c.id") : OrderItem.asc("c.id"));
        UserCart query = new UserCart();
        query.setUserId(userId);
        return Result.success(new PageResult<>(userPageMapper.selectCartList(page, query)).addExtra("cartCount", cartCount));
    }

    @Override
    public Result<PageResult<UserGoods>> pageUserGoods(PageUserGoodsRequest request, Integer userId) {
        Page<UserGoods> page = new Page<>(request.getPage(), request.getSize());
        page.addOrder(Boolean.TRUE.equals(request.getDesc()) ? OrderItem.desc("g.id") : OrderItem.asc("g.id"));
        UserGoods query = new UserGoods();
        query.setUserId(userId);
        query.setUserName(request.getUserName());
        query.setGoodsName(request.getGoodsName());
        query.setPriceMin(request.getPriceMin());
        query.setPriceMax(request.getPriceMax());
        return Result.success(new PageResult<>(userPageMapper.selectGoodsList(page, query)));
    }

    @Override
    public Result<PageResult<UserTrade>> pageUserTrade(PageUserTradeRequest request, Integer userId) {
        Page<UserTrade> page = new Page<>(request.getPage(), request.getSize());
        page.addOrder(Boolean.TRUE.equals(request.getDesc()) ? OrderItem.desc("t.id") : OrderItem.asc("t.id"));
        UserTrade query = new UserTrade();
        query.setUserId(userId);
        query.setMerchantName(request.getMerchantName());
        query.setGoodsName(request.getGoodsName());
        query.setCreateTimeStart(request.getCreateTimeStart());
        query.setCreateTimeEnd(request.getCreateTimeEnd());
        return Result.success(new PageResult<>(userPageMapper.selectTradeList(page, query)));
    }

    @Override
    public Result<PageResult<UserBackground>> pageUserBackground(PageUserBackgroundRequest request, Integer userId) {
        Page<UserBackground> page = new Page<>(request.getPage(), request.getSize());
        UserBackground query = new UserBackground();
        query.setUserId(userId);
        return Result.success(new PageResult<>(userPageMapper.selectBackgroundList(page, query)));
    }

    @Override
    public Result<PageResult<UserVipConfig>> pageUserVipConfig(PageUserVipConfigRequest request) {
        PageResult<UserVipConfig> result = queryCache.getOrLoad(
                CACHE_KEY_VIP_CONFIG + ":" + request.getPage() + ":" + request.getSize(),
                new TypeReference<PageResult<UserVipConfig>>() {},
                () -> {
                    Page<UserVipConfig> page = new Page<>(request.getPage(), request.getSize());
                    return new PageResult<>(userPageMapper.selectVipConfigList(page, new UserVipConfig()));
                },
                CACHE_TTL_MEDIUM
        );
        return Result.success(result);
    }

    @Override
    public Result<PageResult<UserVipTrade>> pageUserVipTrade(PageUserVipTradeRequest request, Integer userId) {
        Page<UserVipTrade> page = new Page<>(request.getPage(), request.getSize());
        page.addOrder(Boolean.TRUE.equals(request.getDesc()) ? OrderItem.desc("vt.id") : OrderItem.asc("vt.id"));
        UserVipTrade query = new UserVipTrade();
        query.setUserId(userId);
        return Result.success(new PageResult<>(userPageMapper.selectVipTradeList(page, query)));
    }

    @Override
    public Result<UserBackgroundVO> userBackground(Integer userId) {
        UserBackgroundVO info = queryCache.getOrLoad(
                CACHE_KEY_USER_BACKGROUND + userId,
                new TypeReference<UserBackgroundVO>() {},
                () -> {
                    UserBackgroundVO vo = userPageMapper.selectUserBackgroundInfo(userId);
                    if (vo == null) fail("用户不存在");
                    vo.setBackgrounds(userPageMapper.selectUserBackgroundImages(userId));
                    vo.setGoods(userPageMapper.selectUserBackgroundGoods(userId));
                    return vo;
                },
                CACHE_TTL_SHORT
        );
        return Result.success(info);
    }
}