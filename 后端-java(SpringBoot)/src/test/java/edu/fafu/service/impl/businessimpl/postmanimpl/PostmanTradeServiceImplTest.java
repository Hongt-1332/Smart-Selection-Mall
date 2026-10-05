package edu.fafu.service.impl.businessimpl.postmanimpl;

import edu.fafu.database.entity.Goods;
import edu.fafu.database.entity.Result;
import edu.fafu.database.entity.Trade;
import edu.fafu.database.mapper.businessmapper.merchantmapper.MerchantGoodsMapper;
import edu.fafu.database.mapper.businessmapper.postmanmapper.PostmanTradeMapper;
import edu.fafu.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("PostmanTradeService 单元测试")
class PostmanTradeServiceImplTest {

    @Mock
    private PostmanTradeMapper postmanTradeMapper;
    @Mock
    private MerchantGoodsMapper merchantGoodsMapper;

    @InjectMocks
    private PostmanTradeServiceImpl postmanTradeService;

    private final Integer merchantId = 2;

    @Test
    @DisplayName("更新当前地址 - 订单ID为空")
    void updateCurrentAddress_tradeIdNull() {
        Trade trade = new Trade();
        trade.setId(null);
        trade.setCurrentAddress("地址");
        BusinessException ex = assertThrows(BusinessException.class, () -> postmanTradeService.updateCurrentAddress(trade, merchantId));
        assertEquals("订单ID不能为空", ex.getMessage());
    }

    @Test
    @DisplayName("更新当前地址 - 当前地址为空")
    void updateCurrentAddress_addressEmpty() {
        Trade trade = new Trade();
        trade.setId(1);
        trade.setCurrentAddress("");
        BusinessException ex = assertThrows(BusinessException.class, () -> postmanTradeService.updateCurrentAddress(trade, merchantId));
        assertEquals("当前地址不能为空", ex.getMessage());
    }

    @Test
    @DisplayName("更新当前地址 - 订单不存在")
    void updateCurrentAddress_tradeNotFound() {
        Trade trade = new Trade();
        trade.setId(999);
        trade.setCurrentAddress("新地址");
        when(postmanTradeMapper.selectById(any(Trade.class))).thenReturn(null);
        BusinessException ex = assertThrows(BusinessException.class, () -> postmanTradeService.updateCurrentAddress(trade, merchantId));
        assertEquals("订单不存在", ex.getMessage());
    }

    @Test
    @DisplayName("更新当前地址 - 商品不属于当前商户")
    void updateCurrentAddress_goodsNotBelongToMerchant() {
        Trade old = new Trade();
        old.setId(1);
        old.setGoodId(50);
        when(postmanTradeMapper.selectById(any(Trade.class))).thenReturn(old);

        Goods goods = new Goods();
        goods.setId(50);
        goods.setUserId(999);
        when(merchantGoodsMapper.selectById(any(Goods.class))).thenReturn(goods);

        Trade trade = new Trade();
        trade.setId(1);
        trade.setCurrentAddress("新地址");
        BusinessException ex = assertThrows(BusinessException.class, () -> postmanTradeService.updateCurrentAddress(trade, merchantId));
        assertEquals("订单商品不属于当前商户", ex.getMessage());
    }

    @Test
    @DisplayName("更新当前地址 - 成功更新")
    void updateCurrentAddress_success() {
        Trade old = new Trade();
        old.setId(1);
        old.setGoodId(50);
        when(postmanTradeMapper.selectById(any(Trade.class))).thenReturn(old);

        Goods goods = new Goods();
        goods.setId(50);
        goods.setUserId(merchantId);
        when(merchantGoodsMapper.selectById(any(Goods.class))).thenReturn(goods);

        Trade trade = new Trade();
        trade.setId(1);
        trade.setCurrentAddress("深圳市南山区");
        Result<String> result = postmanTradeService.updateCurrentAddress(trade, merchantId);
        assertEquals(200, result.getCode());
        assertEquals("更新成功", result.getData());
        verify(postmanTradeMapper).update(any(Trade.class));
    }
}