package edu.fafu.service.impl.businessimpl.merchantimpl;

import edu.fafu.config.SystemConfig;
import edu.fafu.database.entity.Goods;
import edu.fafu.database.entity.Result;
import edu.fafu.database.entity.Trade;
import edu.fafu.database.entity.User;
import edu.fafu.database.mapper.businessmapper.merchantmapper.MerchantGoodsMapper;
import edu.fafu.database.mapper.businessmapper.merchantmapper.MerchantTradeMapper;
import edu.fafu.database.mapper.businessmapper.usermapper.UserUserMapper;
import edu.fafu.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MerchantTradeService 单元测试")
class MerchantTradeServiceImplTest {

    @Mock
    private MerchantTradeMapper merchantTradeMapper;
    @Mock
    private MerchantGoodsMapper merchantGoodsMapper;
    @Mock
    private UserUserMapper userUserMapper;
    @Mock
    private SystemConfig systemConfig;

    @InjectMocks
    private MerchantTradeServiceImpl merchantTradeService;

    private final Integer merchantId = 2;
    private final Integer buyerId = 1;

    @Test
    @DisplayName("商户撤销订单 - 订单不存在")
    void cancelTrade_tradeNotFound() {
        when(merchantTradeMapper.selectById(any(Trade.class))).thenReturn(null);
        BusinessException ex = assertThrows(BusinessException.class, () -> merchantTradeService.cancelTrade(999, merchantId));
        assertEquals("订单不存在", ex.getMessage());
    }

    @Test
    @DisplayName("商户撤销订单 - 订单不属于当前商户")
    void cancelTrade_tradeNotBelongToMerchant() {
        Trade trade = new Trade();
        trade.setId(100);
        trade.setGoodId(50);
        when(merchantTradeMapper.selectById(any(Trade.class))).thenReturn(trade);

        Goods otherGoods = new Goods();
        otherGoods.setId(50);
        otherGoods.setUserId(999);
        when(merchantGoodsMapper.selectById(any(Goods.class))).thenReturn(otherGoods);

        BusinessException ex = assertThrows(BusinessException.class, () -> merchantTradeService.cancelTrade(100, merchantId));
        assertEquals("订单不属于当前商户", ex.getMessage());
    }

    @Test
    @DisplayName("商户撤销订单 - 订单已撤销")
    void cancelTrade_tradeAlreadyCancelled() {
        Trade trade = new Trade();
        trade.setId(100);
        trade.setGoodId(50);
        trade.setCancelTime(LocalDateTime.now());
        when(merchantTradeMapper.selectById(any(Trade.class))).thenReturn(trade);

        Goods goods = new Goods();
        goods.setId(50);
        goods.setUserId(merchantId);
        when(merchantGoodsMapper.selectById(any(Goods.class))).thenReturn(goods);

        BusinessException ex = assertThrows(BusinessException.class, () -> merchantTradeService.cancelTrade(100, merchantId));
        assertEquals("订单已撤销", ex.getMessage());
    }

    @Test
    @DisplayName("商户撤销订单 - 订单已完成无法撤销")
    void cancelTrade_tradeAlreadyFinished() {
        Trade trade = new Trade();
        trade.setId(100);
        trade.setGoodId(50);
        trade.setFinishTime(LocalDateTime.now());
        when(merchantTradeMapper.selectById(any(Trade.class))).thenReturn(trade);

        Goods goods = new Goods();
        goods.setId(50);
        goods.setUserId(merchantId);
        when(merchantGoodsMapper.selectById(any(Goods.class))).thenReturn(goods);

        BusinessException ex = assertThrows(BusinessException.class, () -> merchantTradeService.cancelTrade(100, merchantId));
        assertEquals("订单已完成，无法撤销", ex.getMessage());
    }

    @Test
    @DisplayName("商户撤销订单 - 成功撤销并退款")
    void cancelTrade_success() {
        Trade trade = new Trade();
        trade.setId(100);
        trade.setUserId(buyerId);
        trade.setGoodId(50);
        trade.setQuantity(2);
        trade.setCreateTime(LocalDateTime.now().minusHours(1));
        when(merchantTradeMapper.selectById(any(Trade.class))).thenReturn(trade);

        Goods goods = new Goods();
        goods.setId(50);
        goods.setUserId(merchantId);
        goods.setGoodsPrice(new BigDecimal("99.99"));
        goods.setGoodsStock(10);
        when(merchantGoodsMapper.selectById(any(Goods.class))).thenReturn(goods);
        when(systemConfig.getTradeMerchantCancelTimeoutHours()).thenReturn(24);

        User buyer = new User();
        buyer.setId(buyerId);
        buyer.setBalance(new BigDecimal("100.00"));
        User merchant = new User();
        merchant.setId(merchantId);
        merchant.setBalance(new BigDecimal("500.00"));
        User buyerQuery = new User();
        buyerQuery.setId(buyerId);
        when(userUserMapper.selectById(buyerQuery)).thenReturn(buyer);
        User merchantQuery = new User();
        merchantQuery.setId(merchantId);
        when(userUserMapper.selectById(merchantQuery)).thenReturn(merchant);

        Result<String> result = merchantTradeService.cancelTrade(100, merchantId);
        assertEquals(200, result.getCode());
        assertEquals("撤销成功，已退款", result.getData());
        verify(merchantTradeMapper).update(any(Trade.class));
    }

    @Test
    @DisplayName("商户完成订单 - 订单不存在")
    void finishTrade_tradeNotFound() {
        when(merchantTradeMapper.selectById(any(Trade.class))).thenReturn(null);
        BusinessException ex = assertThrows(BusinessException.class, () -> merchantTradeService.finishTrade(999, merchantId));
        assertEquals("订单不存在", ex.getMessage());
    }

    @Test
    @DisplayName("商户完成订单 - 成功完成")
    void finishTrade_success() {
        Trade trade = new Trade();
        trade.setId(100);
        trade.setGoodId(50);
        when(merchantTradeMapper.selectById(any(Trade.class))).thenReturn(trade);

        Goods goods = new Goods();
        goods.setId(50);
        goods.setUserId(merchantId);
        when(merchantGoodsMapper.selectById(any(Goods.class))).thenReturn(goods);

        Result<String> result = merchantTradeService.finishTrade(100, merchantId);
        assertEquals(200, result.getCode());
        assertEquals("完成成功", result.getData());
        verify(merchantTradeMapper).update(any(Trade.class));
    }

    @Test
    @DisplayName("商户完成订单 - 订单已撤销无法完成")
    void finishTrade_tradeAlreadyCancelled() {
        Trade trade = new Trade();
        trade.setId(100);
        trade.setGoodId(50);
        trade.setCancelTime(LocalDateTime.now());
        when(merchantTradeMapper.selectById(any(Trade.class))).thenReturn(trade);

        Goods goods = new Goods();
        goods.setId(50);
        goods.setUserId(merchantId);
        when(merchantGoodsMapper.selectById(any(Goods.class))).thenReturn(goods);

        BusinessException ex = assertThrows(BusinessException.class, () -> merchantTradeService.finishTrade(100, merchantId));
        assertEquals("订单已撤销，无法完成", ex.getMessage());
    }

    @Test
    @DisplayName("获取未完成订单数")
    void getUnfinishedCount() {
        when(merchantTradeMapper.countUnfinished(merchantId)).thenReturn(3);
        Result<Integer> result = merchantTradeService.getUnfinishedCount(merchantId);
        assertEquals(200, result.getCode());
        assertEquals(3, result.getData());
    }
}