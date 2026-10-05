package edu.fafu.service.impl.businessimpl.userimpl;

import edu.fafu.database.entity.*;
import edu.fafu.database.dto.request.user.BuyGoodsRequest;
import edu.fafu.database.mapper.businessmapper.usermapper.UserAddressMapper;
import edu.fafu.database.mapper.businessmapper.usermapper.UserGoodsMapper;
import edu.fafu.database.mapper.businessmapper.usermapper.UserTradeMapper;
import edu.fafu.database.mapper.businessmapper.usermapper.UserUserMapper;
import edu.fafu.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserGoodsService 单元测试")
class UserGoodsServiceImplTest {

    @Mock
    private UserGoodsMapper userGoodsMapper;
    @Mock
    private UserUserMapper userUserMapper;
    @Mock
    private UserAddressMapper userAddressMapper;
    @Mock
    private UserTradeMapper userTradeMapper;

    @InjectMocks
    private UserGoodsServiceImpl userGoodsService;

    private final Integer userId = 1;
    private final Integer merchantId = 2;

    @Test
    @DisplayName("购买商品 - 商品ID为空时selectById返回null，抛出商品不存在")
    void buyGoods_goodsIdNull() {
        when(userGoodsMapper.selectById(any(Goods.class))).thenReturn(null);
        BuyGoodsRequest request = new BuyGoodsRequest();
        request.setGoodsId(null);
        request.setQuantity(1);
        request.setShippingAddressId(1);
        BusinessException ex = assertThrows(BusinessException.class, () -> userGoodsService.buyGoods(request, userId));
        assertEquals("商品不存在", ex.getMessage());
    }

    @Test
    @DisplayName("购买商品 - 收货地址为空时selectById返回null，抛出收货地址不存在")
    void buyGoods_addressIdNull() {
        Goods goods = new Goods();
        goods.setId(1);
        goods.setGoodsStock(10);
        goods.setLaunch(true);
        when(userGoodsMapper.selectById(any(Goods.class))).thenReturn(goods);
        when(userAddressMapper.selectById(any(Address.class))).thenReturn(null);

        BuyGoodsRequest request = new BuyGoodsRequest();
        request.setGoodsId(1);
        request.setQuantity(1);
        request.setShippingAddressId(null);
        BusinessException ex = assertThrows(BusinessException.class, () -> userGoodsService.buyGoods(request, userId));
        assertEquals("收货地址不存在", ex.getMessage());
    }

    @Test
    @DisplayName("购买商品 - 商品不存在")
    void buyGoods_goodsNotFound() {
        BuyGoodsRequest request = new BuyGoodsRequest();
        request.setGoodsId(999);
        request.setQuantity(1);
        request.setShippingAddressId(1);
        when(userGoodsMapper.selectById(any(Goods.class))).thenReturn(null);
        BusinessException ex = assertThrows(BusinessException.class, () -> userGoodsService.buyGoods(request, userId));
        assertEquals("商品不存在", ex.getMessage());
    }

    @Test
    @DisplayName("购买商品 - 库存不足")
    void buyGoods_stockNotEnough() {
        Goods goods = new Goods();
        goods.setId(1);
        goods.setGoodsStock(0);
        goods.setLaunch(true);
        when(userGoodsMapper.selectById(any(Goods.class))).thenReturn(goods);

        BuyGoodsRequest request = new BuyGoodsRequest();
        request.setGoodsId(1);
        request.setQuantity(1);
        request.setShippingAddressId(1);
        BusinessException ex = assertThrows(BusinessException.class, () -> userGoodsService.buyGoods(request, userId));
        assertEquals("库存不足", ex.getMessage());
    }

    @Test
    @DisplayName("购买商品 - 商品已下架")
    void buyGoods_goodsOffShelf() {
        Goods goods = new Goods();
        goods.setId(1);
        goods.setGoodsStock(10);
        goods.setLaunch(false);
        when(userGoodsMapper.selectById(any(Goods.class))).thenReturn(goods);

        BuyGoodsRequest request = new BuyGoodsRequest();
        request.setGoodsId(1);
        request.setQuantity(1);
        request.setShippingAddressId(1);
        BusinessException ex = assertThrows(BusinessException.class, () -> userGoodsService.buyGoods(request, userId));
        assertEquals("商品已下架", ex.getMessage());
    }

    @Test
    @DisplayName("购买商品 - 收货地址不存在")
    void buyGoods_addressNotFound() {
        Goods goods = new Goods();
        goods.setId(1);
        goods.setGoodsStock(10);
        goods.setLaunch(true);
        when(userGoodsMapper.selectById(any(Goods.class))).thenReturn(goods);
        when(userAddressMapper.selectById(any(Address.class))).thenReturn(null);

        BuyGoodsRequest request = new BuyGoodsRequest();
        request.setGoodsId(1);
        request.setQuantity(1);
        request.setShippingAddressId(999);
        BusinessException ex = assertThrows(BusinessException.class, () -> userGoodsService.buyGoods(request, userId));
        assertEquals("收货地址不存在", ex.getMessage());
    }

    @Test
    @DisplayName("购买商品 - 收货地址不属于当前用户")
    void buyGoods_addressNotBelongToUser() {
        Goods goods = new Goods();
        goods.setId(1);
        goods.setGoodsStock(10);
        goods.setLaunch(true);
        when(userGoodsMapper.selectById(any(Goods.class))).thenReturn(goods);

        Address address = new Address();
        address.setId(1);
        address.setUserId(999);
        when(userAddressMapper.selectById(any(Address.class))).thenReturn(address);

        BuyGoodsRequest request = new BuyGoodsRequest();
        request.setGoodsId(1);
        request.setQuantity(1);
        request.setShippingAddressId(1);
        BusinessException ex = assertThrows(BusinessException.class, () -> userGoodsService.buyGoods(request, userId));
        assertEquals("收货地址不属于当前用户", ex.getMessage());
    }

    @Test
    @DisplayName("购买商品 - 余额不足")
    void buyGoods_balanceNotEnough() {
        Goods goods = new Goods();
        goods.setId(1);
        goods.setUserId(merchantId);
        goods.setGoodsStock(10);
        goods.setGoodsPrice(new BigDecimal("999.99"));
        goods.setLaunch(true);
        when(userGoodsMapper.selectById(any(Goods.class))).thenReturn(goods);

        Address address = new Address();
        address.setId(1);
        address.setUserId(userId);
        when(userAddressMapper.selectById(any(Address.class))).thenReturn(address);

        User buyer = new User();
        buyer.setId(userId);
        buyer.setBalance(new BigDecimal("100.00"));
        when(userUserMapper.selectById(any(User.class))).thenReturn(buyer);

        BuyGoodsRequest request = new BuyGoodsRequest();
        request.setGoodsId(1);
        request.setQuantity(1);
        request.setShippingAddressId(1);
        BusinessException ex = assertThrows(BusinessException.class, () -> userGoodsService.buyGoods(request, userId));
        assertEquals("余额不足", ex.getMessage());
    }

    @Test
    @DisplayName("购买商品 - 成功购买")
    void buyGoods_success() {
        Goods goods = new Goods();
        goods.setId(1);
        goods.setUserId(merchantId);
        goods.setGoodsStock(10);
        goods.setGoodsPrice(new BigDecimal("50.00"));
        goods.setLaunch(true);
        when(userGoodsMapper.selectById(any(Goods.class))).thenReturn(goods);

        Address address = new Address();
        address.setId(1);
        address.setUserId(userId);
        address.setDetail("测试地址");
        when(userAddressMapper.selectById(any(Address.class))).thenReturn(address);

        User buyer = new User();
        buyer.setId(userId);
        buyer.setBalance(new BigDecimal("500.00"));
        User merchant = new User();
        merchant.setId(merchantId);
        merchant.setBalance(new BigDecimal("200.00"));
        User buyerQuery = new User();
        buyerQuery.setId(userId);
        when(userUserMapper.selectById(buyerQuery)).thenReturn(buyer);
        User merchantQuery = new User();
        merchantQuery.setId(merchantId);
        when(userUserMapper.selectById(merchantQuery)).thenReturn(merchant);

        BuyGoodsRequest request = new BuyGoodsRequest();
        request.setGoodsId(1);
        request.setQuantity(2);
        request.setShippingAddressId(1);
        Result<String> result = userGoodsService.buyGoods(request, userId);
        assertEquals(200, result.getCode());
        assertEquals("购买成功", result.getData());

        verify(userTradeMapper).insert(any(Trade.class));
        verify(userGoodsMapper).update(any(Goods.class));
        verify(userUserMapper, times(2)).update(any(User.class));
    }
}