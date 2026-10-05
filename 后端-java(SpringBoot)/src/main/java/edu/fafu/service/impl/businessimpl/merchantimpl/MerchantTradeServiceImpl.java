package edu.fafu.service.impl.businessimpl.merchantimpl;

import edu.fafu.config.SystemConfig;
import edu.fafu.database.entity.Goods;
import edu.fafu.database.entity.Result;
import edu.fafu.database.entity.Trade;
import edu.fafu.database.entity.User;
import edu.fafu.database.mapper.businessmapper.merchantmapper.MerchantGoodsMapper;
import edu.fafu.database.mapper.businessmapper.merchantmapper.MerchantTradeMapper;
import edu.fafu.database.mapper.businessmapper.usermapper.UserUserMapper;
import edu.fafu.exception.BusinessExceptionInterface;
import edu.fafu.service.service.businessservice.merchantservice.MerchantTradeService;
import edu.fafu.tool.cache.QueryCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
public class MerchantTradeServiceImpl implements MerchantTradeService, BusinessExceptionInterface {

    private static final String CACHE_KEY_UNFINISHED = "merchant:trade:unfinished:";

    @Autowired
    private MerchantTradeMapper merchantTradeMapper;
    @Autowired
    private MerchantGoodsMapper merchantGoodsMapper;
    @Autowired
    private UserUserMapper userUserMapper;
    @Autowired
    private SystemConfig systemConfig;
    @Autowired
    private QueryCache queryCache;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> cancelTrade(Integer id, Integer userId) {
        Trade exist = new Trade();
        exist.setId(id);
        Trade old = merchantTradeMapper.selectById(exist);
        ensureNotNull(old, "订单不存在");

        Goods goodsQuery = new Goods();
        goodsQuery.setId(old.getGoodId());
        Goods goods = merchantGoodsMapper.selectById(goodsQuery);
        ensureTrue(goods != null && goods.getUserId().equals(userId), "订单不属于当前商户");

        ensureNull(old.getCancelTime(), "订单已撤销");
        ensureNull(old.getFinishTime(), "订单已完成，无法撤销");

        ensureNotNull(old.getCreateTime(), "订单创建时间异常");
        long hours = ChronoUnit.HOURS.between(old.getCreateTime(), LocalDateTime.now());
        ensureTrue(hours <= systemConfig.getTradeMerchantCancelTimeoutHours(), "订单已超过" + systemConfig.getTradeMerchantCancelTimeoutHours() + "小时，无法撤销");

        BigDecimal refundAmount = BigDecimal.ZERO;
        if (goods.getGoodsPrice() != null && old.getQuantity() != null) {
            refundAmount = goods.getGoodsPrice().multiply(BigDecimal.valueOf(old.getQuantity()));
        }

        if (refundAmount.compareTo(BigDecimal.ZERO) > 0) {
            User buyerQuery = new User();
            buyerQuery.setId(old.getUserId());
            User buyer = userUserMapper.selectById(buyerQuery);
            if (buyer != null) {
                buyer.setBalance(buyer.getBalance().add(refundAmount));
                userUserMapper.update(buyer);
            }

            User merchantQuery = new User();
            merchantQuery.setId(goods.getUserId());
            User merchant = userUserMapper.selectById(merchantQuery);
            if (merchant != null) {
                merchant.setBalance(merchant.getBalance().subtract(refundAmount));
                userUserMapper.update(merchant);
            }
        }

        if (old.getQuantity() != null) {
            goods.setGoodsStock(goods.getGoodsStock() + old.getQuantity());
            merchantGoodsMapper.update(goods);
        }

        Trade update = new Trade();
        update.setId(id);
        update.setCancelTime(LocalDateTime.now());
        merchantTradeMapper.update(update);
        return Result.success("撤销成功，已退款");
    }

    @Override
    public Result<String> finishTrade(Integer id, Integer userId) {
        Trade exist = new Trade();
        exist.setId(id);
        Trade old = merchantTradeMapper.selectById(exist);
        ensureNotNull(old, "订单不存在");

        Goods goodsQuery = new Goods();
        goodsQuery.setId(old.getGoodId());
        Goods goods = merchantGoodsMapper.selectById(goodsQuery);
        ensureTrue(goods != null && goods.getUserId().equals(userId), "订单不属于当前商户");

        ensureNull(old.getCancelTime(), "订单已撤销，无法完成");
        ensureNull(old.getFinishTime(), "订单已完成");

        Trade update = new Trade();
        update.setId(id);
        update.setFinishTime(LocalDateTime.now());
        merchantTradeMapper.update(update);
        return Result.success("完成成功");
    }

    @Override
    public Result<Integer> getUnfinishedCount(Integer userId) {
        return Result.success(queryCache.getOrLoadInt(
                CACHE_KEY_UNFINISHED + userId,
                () -> merchantTradeMapper.countUnfinished(userId),
                1
        ));
    }

    @Override
    public Result<Trade> getTrade(Integer id, Integer userId) {
        Trade exist = new Trade();
        exist.setId(id);
        Trade trade = merchantTradeMapper.selectById(exist);
        ensureNotNull(trade, "订单不存在");

        Goods goodsQuery = new Goods();
        goodsQuery.setId(trade.getGoodId());
        Goods goods = merchantGoodsMapper.selectById(goodsQuery);
        ensureTrue(goods != null && goods.getUserId().equals(userId), "订单不属于当前商户");

        return Result.success(trade);
    }

    @Override
    public Result<String> deliverTrade(Integer id, Integer userId) {
        Trade exist = new Trade();
        exist.setId(id);
        Trade trade = merchantTradeMapper.selectById(exist);
        ensureNotNull(trade, "订单不存在");

        Goods goodsQuery = new Goods();
        goodsQuery.setId(trade.getGoodId());
        Goods goods = merchantGoodsMapper.selectById(goodsQuery);
        ensureTrue(goods != null && goods.getUserId().equals(userId), "订单不属于当前商户");

        ensureNull(trade.getCancelTime(), "订单已撤销");
        ensureNull(trade.getFinishTime(), "订单已完成");

        Trade update = new Trade();
        update.setId(id);
        update.setCurrentAddress(trade.getTargetAddress());
        merchantTradeMapper.update(update);
        return Result.success("发货成功");
    }

    @Override
    public Result<String> confirmTrade(Integer id, Integer userId) {
        Trade exist = new Trade();
        exist.setId(id);
        Trade trade = merchantTradeMapper.selectById(exist);
        ensureNotNull(trade, "订单不存在");

        Goods goodsQuery = new Goods();
        goodsQuery.setId(trade.getGoodId());
        Goods goods = merchantGoodsMapper.selectById(goodsQuery);
        ensureTrue(goods != null && goods.getUserId().equals(userId), "订单不属于当前商户");

        ensureNull(trade.getCancelTime(), "订单已撤销");
        ensureNull(trade.getFinishTime(), "订单已完成");

        Trade update = new Trade();
        update.setId(id);
        update.setFinishTime(LocalDateTime.now());
        merchantTradeMapper.update(update);
        return Result.success("确认成功");
    }

    @Override
    public Result<String> deleteTrade(Integer id, Integer userId) {
        Trade exist = new Trade();
        exist.setId(id);
        Trade trade = merchantTradeMapper.selectById(exist);
        ensureNotNull(trade, "订单不存在");

        Goods goodsQuery = new Goods();
        goodsQuery.setId(trade.getGoodId());
        Goods goods = merchantGoodsMapper.selectById(goodsQuery);
        ensureTrue(goods != null && goods.getUserId().equals(userId), "订单不属于当前商户");

        Trade update = new Trade();
        update.setId(id);
        update.setDelete(true);
        merchantTradeMapper.update(update);
        return Result.success("删除成功");
    }
}