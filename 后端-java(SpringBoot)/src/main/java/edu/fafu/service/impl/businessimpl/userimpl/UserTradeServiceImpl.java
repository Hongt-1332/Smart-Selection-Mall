package edu.fafu.service.impl.businessimpl.userimpl;

import edu.fafu.config.SystemConfig;
import edu.fafu.database.entity.Goods;
import edu.fafu.database.entity.Result;
import edu.fafu.database.entity.Trade;
import edu.fafu.database.entity.User;
import edu.fafu.database.mapper.businessmapper.usermapper.UserGoodsMapper;
import edu.fafu.database.mapper.businessmapper.usermapper.UserTradeMapper;
import edu.fafu.database.mapper.businessmapper.usermapper.UserUserMapper;
import edu.fafu.exception.BusinessExceptionInterface;
import edu.fafu.service.service.businessservice.userservice.UserTradeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
public class UserTradeServiceImpl implements UserTradeService, BusinessExceptionInterface {

    @Autowired
    private UserTradeMapper userTradeMapper;
    @Autowired
    private UserUserMapper userUserMapper;
    @Autowired
    private UserGoodsMapper userGoodsMapper;
    @Autowired
    private SystemConfig systemConfig;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> cancelTrade(Integer id, Integer userId) {
        Trade query = new Trade();
        query.setId(id);
        Trade trade = userTradeMapper.selectById(query);
        ensureNotNull(trade, "订单不存在");
        ensureEquals(trade.getUserId(), userId, "订单不属于当前用户");
        ensureNull(trade.getCancelTime(), "订单已撤销");
        ensureNull(trade.getFinishTime(), "订单已完成，无法撤销");

        LocalDateTime createTime = trade.getCreateTime();
        ensureNotNull(createTime, "订单创建时间异常");

        long minutes = ChronoUnit.MINUTES.between(createTime, LocalDateTime.now());
        ensureTrue(minutes <= systemConfig.getTradeCancelTimeoutMinutes(), "订单已超过" + systemConfig.getTradeCancelTimeoutMinutes() + "分钟，无法撤销");

        Goods goodsQuery = new Goods();
        goodsQuery.setId(trade.getGoodId());
        Goods goods = userGoodsMapper.selectById(goodsQuery);

        BigDecimal refundAmount = BigDecimal.ZERO;
        if (goods != null && goods.getGoodsPrice() != null && trade.getQuantity() != null) {
            refundAmount = goods.getGoodsPrice().multiply(BigDecimal.valueOf(trade.getQuantity()));
        }

        if (refundAmount.compareTo(BigDecimal.ZERO) > 0) {
            User buyerQuery = new User();
            buyerQuery.setId(userId);
            User buyer = userUserMapper.selectById(buyerQuery);
            if (buyer != null) {
                buyer.setBalance(buyer.getBalance().add(refundAmount));
                userUserMapper.update(buyer);
            }

            if (goods != null) {
                User merchantQuery = new User();
                merchantQuery.setId(goods.getUserId());
                User merchant = userUserMapper.selectById(merchantQuery);
                if (merchant != null) {
                    merchant.setBalance(merchant.getBalance().subtract(refundAmount));
                    userUserMapper.update(merchant);
                }
            }
        }

        if (goods != null && trade.getQuantity() != null) {
            goods.setGoodsStock(goods.getGoodsStock() + trade.getQuantity());
            userGoodsMapper.update(goods);
        }

        Trade update = new Trade();
        update.setId(id);
        update.setCancelTime(LocalDateTime.now());
        userTradeMapper.update(update);
        return Result.success("撤销成功，已退款");
    }
}