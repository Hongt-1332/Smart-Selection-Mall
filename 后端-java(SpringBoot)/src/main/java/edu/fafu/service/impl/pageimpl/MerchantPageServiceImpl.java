package edu.fafu.service.impl.pageimpl;

import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import edu.fafu.database.entity.Result;
import edu.fafu.database.dto.request.page.*;
import edu.fafu.database.dto.response.merchant.*;
import edu.fafu.database.mapper.pagemapper.MerchantPageMapper;
import edu.fafu.exception.BusinessExceptionInterface;
import edu.fafu.service.service.pageservice.MerchantPageService;
import edu.fafu.tool.cache.QueryCache;
import edu.fafu.tool.page.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tools.jackson.core.type.TypeReference;

@Service
public class MerchantPageServiceImpl implements MerchantPageService, BusinessExceptionInterface {

    private static final String CACHE_KEY_MERCHANT_BACKGROUND = "page:merchant:background:";
    private static final long CACHE_TTL_SHORT = 2;

    @Autowired
    private MerchantPageMapper merchantPageMapper;
    @Autowired
    private QueryCache queryCache;

    @Override
    public Result<PageResult<MerchantGoods>> pageMerchantGoods(PageMerchantGoodsRequest request, Integer userId) {
        Page<MerchantGoods> page = new Page<>(request.getPage(), request.getSize());
        page.addOrder(Boolean.TRUE.equals(request.getDesc()) ? OrderItem.desc("id") : OrderItem.asc("id"));
        MerchantGoods query = new MerchantGoods();
        query.setUserId(userId);
        query.setLaunch(request.getLaunch());
        return Result.success(new PageResult<>(merchantPageMapper.selectGoodsList(page, query)));
    }

    @Override
    public Result<PageResult<MerchantTrade>> pageMerchantTrade(PageMerchantTradeRequest request, Integer userId) {
        int unfinishedCount = merchantPageMapper.selectUnfinishedTradeCount(userId);
        Page<MerchantTrade> page = new Page<>(request.getPage(), request.getSize());
        page.addOrder(Boolean.TRUE.equals(request.getDesc()) ? OrderItem.desc("t.id") : OrderItem.asc("t.id"));
        MerchantTrade query = new MerchantTrade();
        query.setMerchantUserId(userId);
        query.setUserName(request.getUserName());
        query.setGoodsName(request.getGoodsName());
        query.setOriginAddress(request.getOriginAddress());
        query.setTargetAddress(request.getTargetAddress());
        query.setCurrentAddress(request.getCurrentAddress());
        query.setPriceMin(request.getPriceMin());
        query.setPriceMax(request.getPriceMax());
        query.setFinishTimeStart(request.getFinishTimeStart());
        query.setFinishTimeEnd(request.getFinishTimeEnd());
        return Result.success(new PageResult<>(merchantPageMapper.selectTradeList(page, query)).addExtra("unfinishedCount", unfinishedCount));
    }

    @Override
    public Result<PageResult<MerchantUser>> pageMerchantUser(PageMerchantUserRequest request) {
        Page<MerchantUser> page = new Page<>(request.getPage(), request.getSize());
        page.addOrder(Boolean.TRUE.equals(request.getDesc()) ? OrderItem.desc("u.id") : OrderItem.asc("u.id"));
        MerchantUser query = new MerchantUser();
        query.setUserName(request.getUserName());
        return Result.success(new PageResult<>(merchantPageMapper.selectMerchantUserList(page, query)));
    }

    @Override
    public Result<PageResult<MerchantBackground>> pageMerchantBackground(PageMerchantBackgroundRequest request, Integer userId) {
        Page<MerchantBackground> page = new Page<>(request.getPage(), request.getSize());
        MerchantBackground query = new MerchantBackground();
        query.setUserId(userId);
        return Result.success(new PageResult<>(merchantPageMapper.selectBackgroundList(page, query)));
    }

    @Override
    public Result<MerchantBackgroundVO> merchantBackground(Integer userId, Integer currentUserId) {
        MerchantBackgroundVO info = queryCache.getOrLoad(
                CACHE_KEY_MERCHANT_BACKGROUND + userId,
                new TypeReference<MerchantBackgroundVO>() {},
                () -> {
                    MerchantBackgroundVO vo = merchantPageMapper.selectMerchantBackgroundInfo(userId);
                    if (vo == null) fail("用户不存在");
                    vo.setBackgrounds(merchantPageMapper.selectMerchantBackgroundImages(userId));
                    vo.setGoods(merchantPageMapper.selectMerchantLaunchGoods(userId, currentUserId));
                    return vo;
                },
                CACHE_TTL_SHORT
        );
        return Result.success(info);
    }

    @Override
    public Result<PageResult<MerchantAi>> pageMerchantAi(PageMerchantAiRequest request, Integer userId) {
        Page<MerchantAi> page = new Page<>(request.getPage(), request.getSize());
        page.addOrder(Boolean.TRUE.equals(request.getDesc()) ? OrderItem.desc("a.id") : OrderItem.asc("a.id"));
        MerchantAi query = new MerchantAi();
        query.setUserId(userId);
        return Result.success(new PageResult<>(merchantPageMapper.selectAiList(page, query)));
    }
}