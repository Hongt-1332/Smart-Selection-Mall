package edu.fafu.service.impl.businessimpl.userimpl;

import edu.fafu.config.SystemConfig;
import edu.fafu.database.entity.Goods;
import edu.fafu.database.entity.Result;
import edu.fafu.database.entity.Trade;
import edu.fafu.database.entity.User;
import edu.fafu.database.mapper.businessmapper.usermapper.UserGoodsMapper;
import edu.fafu.database.mapper.businessmapper.usermapper.UserTradeMapper;
import edu.fafu.database.mapper.businessmapper.usermapper.UserUserMapper;
import edu.fafu.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
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
@DisplayName("UserTradeService 单元测试")
class UserTradeServiceImplTest {

    @Mock
    private UserTradeMapper userTradeMapper;
    @Mock
    private UserUserMapper userUserMapper;
    @Mock
    private UserGoodsMapper userGoodsMapper;
    @Mock
    private SystemConfig systemConfig;

    @InjectMocks
    private UserTradeServiceImpl userTradeService;

    private Trade trade;
    private Goods goods;
    private User buyer;
    private User merchant;
    private final Integer userId = 1;
    private final Integer merchantId = 2;

    @BeforeEach
    void setUp() {
        trade = new Trade();
        trade.setId(100);
        trade.setUserId(userId);
        trade.setGoodId(50);
        trade.setQuantity(2);
        trade.setCreateTime(LocalDateTime.now().minusMinutes(5));

        goods = new Goods();
        goods.setId(50);
        goods.setUserId(merchantId);
        goods.setGoodsPrice(new BigDecimal("99.99"));
        goods.setGoodsStock(10);

        buyer = new User();
        buyer.setId(userId);
        buyer.setBalance(new BigDecimal("500.00"));

        merchant = new User();
        merchant.setId(merchantId);
        merchant.setBalance(new BigDecimal("200.00"));
    }

    @Test
    @DisplayName("撤销订单 - 订单不存在")
    void cancelTrade_tradeNotFound() {
        when(userTradeMapper.selectById(any(Trade.class))).thenReturn(null);
        BusinessException ex = assertThrows(BusinessException.class, () -> userTradeService.cancelTrade(999, userId));
        assertEquals("订单不存在", ex.getMessage());
    }

    @Test
    @DisplayName("撤销订单 - 订单不属于当前用户")
    void cancelTrade_tradeNotBelongToUser() {
        trade.setUserId(999);
        when(userTradeMapper.selectById(any(Trade.class))).thenReturn(trade);
        BusinessException ex = assertThrows(BusinessException.class, () -> userTradeService.cancelTrade(100, userId));
        assertEquals("订单不属于当前用户", ex.getMessage());
    }

    @Test
    @DisplayName("撤销订单 - 订单已撤销")
    void cancelTrade_tradeAlreadyCancelled() {
        trade.setCancelTime(LocalDateTime.now());
        when(userTradeMapper.selectById(any(Trade.class))).thenReturn(trade);
        BusinessException ex = assertThrows(BusinessException.class, () -> userTradeService.cancelTrade(100, userId));
        assertEquals("订单已撤销", ex.getMessage());
    }

    @Test
    @DisplayName("撤销订单 - 订单已完成无法撤销")
    void cancelTrade_tradeAlreadyFinished() {
        trade.setFinishTime(LocalDateTime.now());
        when(userTradeMapper.selectById(any(Trade.class))).thenReturn(trade);
        BusinessException ex = assertThrows(BusinessException.class, () -> userTradeService.cancelTrade(100, userId));
        assertEquals("订单已完成，无法撤销", ex.getMessage());
    }

    @Test
    @DisplayName("撤销订单 - 超时无法撤销")
    void cancelTrade_tradeTimeout() {
        trade.setCreateTime(LocalDateTime.now().minusMinutes(999));
        when(userTradeMapper.selectById(any(Trade.class))).thenReturn(trade);
        when(systemConfig.getTradeCancelTimeoutMinutes()).thenReturn(30);
        BusinessException ex = assertThrows(BusinessException.class, () -> userTradeService.cancelTrade(100, userId));
        assertTrue(ex.getMessage().contains("无法撤销"));
    }

    private void mockCancelTradeSuccess() {
        when(userTradeMapper.selectById(any(Trade.class))).thenReturn(trade);
        when(userGoodsMapper.selectById(any(Goods.class))).thenReturn(goods);
        when(systemConfig.getTradeCancelTimeoutMinutes()).thenReturn(30);
        when(userUserMapper.selectById(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            if (u.getId().equals(userId)) return buyer;
            if (u.getId().equals(merchantId)) return merchant;
            return null;
        });
    }

    @Test
    @DisplayName("撤销订单 - 成功撤销并退款")
    void cancelTrade_success() {
        mockCancelTradeSuccess();

        Result<String> result = userTradeService.cancelTrade(100, userId);
        assertEquals(200, result.getCode());
        assertEquals("撤销成功，已退款", result.getData());

        verify(userTradeMapper).update(any(Trade.class));
        verify(userUserMapper, times(2)).update(any(User.class));
        verify(userGoodsMapper).update(any(Goods.class));
    }

    @Test
    @DisplayName("撤销订单 - 退款金额正确（单价×数量）")
    void cancelTrade_refundAmountCorrect() {
        mockCancelTradeSuccess();

        BigDecimal buyerBalanceBefore = buyer.getBalance();
        BigDecimal merchantBalanceBefore = merchant.getBalance();
        BigDecimal expectedRefund = goods.getGoodsPrice().multiply(BigDecimal.valueOf(trade.getQuantity()));

        userTradeService.cancelTrade(100, userId);

        assertEquals(buyerBalanceBefore.add(expectedRefund), buyer.getBalance());
        assertEquals(merchantBalanceBefore.subtract(expectedRefund), merchant.getBalance());
    }

    @Test
    @DisplayName("撤销订单 - 库存恢复正确")
    void cancelTrade_stockRestored() {
        mockCancelTradeSuccess();

        int stockBefore = goods.getGoodsStock();
        userTradeService.cancelTrade(100, userId);
        assertEquals(stockBefore + trade.getQuantity(), goods.getGoodsStock());
    }
}