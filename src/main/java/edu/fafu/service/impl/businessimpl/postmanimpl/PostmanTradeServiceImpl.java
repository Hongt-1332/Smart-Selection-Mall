package edu.fafu.service.impl.businessimpl.postmanimpl;

import edu.fafu.database.entity.Goods;
import edu.fafu.database.entity.Result;
import edu.fafu.database.entity.Trade;
import edu.fafu.database.mapper.businessmapper.postmanmapper.PostmanTradeMapper;
import edu.fafu.exception.BusinessExceptionInterface;
import edu.fafu.service.service.businessservice.postmanservice.PostmanTradeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class PostmanTradeServiceImpl implements PostmanTradeService, BusinessExceptionInterface {

    @Autowired
    private PostmanTradeMapper postmanTradeMapper;
    @Autowired
    private edu.fafu.database.mapper.businessmapper.merchantmapper.MerchantGoodsMapper merchantGoodsMapper;

    @Override
    public Result<String> updateCurrentAddress(Trade trade, Integer userId) {
        ensureNotNull(trade.getId(), "订单ID不能为空");
        ensureNotBlank(trade.getCurrentAddress(), "当前地址不能为空");

        Trade exist = new Trade();
        exist.setId(trade.getId());
        Trade old = postmanTradeMapper.selectById(exist);
        ensureNotNull(old, "订单不存在");

        Goods goodsQuery = new Goods();
        goodsQuery.setId(old.getGoodId());
        Goods goods = merchantGoodsMapper.selectById(goodsQuery);
        ensureTrue(goods != null && goods.getUserId().equals(userId), "订单商品不属于当前商户");

        Trade update = new Trade();
        update.setId(trade.getId());
        update.setCurrentAddress(trade.getCurrentAddress());
        postmanTradeMapper.update(update);
        return Result.success("更新成功");
    }

    @Override
    public Result<String> deliverTrade(Integer id, Integer userId) {
        Trade exist = new Trade();
        exist.setId(id);
        Trade trade = postmanTradeMapper.selectById(exist);
        ensureNotNull(trade, "订单不存在");

        Goods goodsQuery = new Goods();
        goodsQuery.setId(trade.getGoodId());
        Goods goods = merchantGoodsMapper.selectById(goodsQuery);
        ensureTrue(goods != null && goods.getUserId().equals(userId), "订单商品不属于当前商户");

        ensureNull(trade.getCancelTime(), "订单已撤销");
        ensureNull(trade.getFinishTime(), "订单已完成");

        Trade update = new Trade();
        update.setId(id);
        update.setCurrentAddress(trade.getTargetAddress());
        postmanTradeMapper.update(update);
        return Result.success("发货成功");
    }

    @Override
    public Result<String> confirmTrade(Integer id, Integer userId) {
        Trade exist = new Trade();
        exist.setId(id);
        Trade trade = postmanTradeMapper.selectById(exist);
        ensureNotNull(trade, "订单不存在");

        Goods goodsQuery = new Goods();
        goodsQuery.setId(trade.getGoodId());
        Goods goods = merchantGoodsMapper.selectById(goodsQuery);
        ensureTrue(goods != null && goods.getUserId().equals(userId), "订单商品不属于当前商户");

        ensureNull(trade.getCancelTime(), "订单已撤销");
        ensureNull(trade.getFinishTime(), "订单已完成");

        Trade update = new Trade();
        update.setId(id);
        update.setFinishTime(LocalDateTime.now());
        postmanTradeMapper.update(update);
        return Result.success("确认成功");
    }

    @Override
    public Result<String> deleteTrade(Integer id, Integer userId) {
        Trade exist = new Trade();
        exist.setId(id);
        Trade trade = postmanTradeMapper.selectById(exist);
        ensureNotNull(trade, "订单不存在");

        Goods goodsQuery = new Goods();
        goodsQuery.setId(trade.getGoodId());
        Goods goods = merchantGoodsMapper.selectById(goodsQuery);
        ensureTrue(goods != null && goods.getUserId().equals(userId), "订单商品不属于当前商户");

        Trade update = new Trade();
        update.setId(id);
        update.setDelete(true);
        postmanTradeMapper.update(update);
        return Result.success("删除成功");
    }
}