package edu.fafu.service.impl.businessimpl.userimpl;

import edu.fafu.config.VipConfigCache;
import edu.fafu.database.entity.Cart;
import edu.fafu.database.entity.Goods;
import edu.fafu.database.entity.Result;
import edu.fafu.database.entity.User;
import edu.fafu.database.dto.request.user.CartActionRequest;
import edu.fafu.database.mapper.businessmapper.usermapper.UserCartMapper;
import edu.fafu.database.mapper.businessmapper.usermapper.UserGoodsMapper;
import edu.fafu.database.mapper.businessmapper.usermapper.UserUserMapper;
import edu.fafu.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserCartService 单元测试")
class UserCartServiceImplTest {

    @Mock
    private UserCartMapper userCartMapper;
    @Mock
    private UserGoodsMapper userGoodsMapper;
    @Mock
    private UserUserMapper userUserMapper;
    @Mock
    private VipConfigCache vipConfigCache;

    @InjectMocks
    private UserCartServiceImpl userCartService;

    private final Integer userId = 1;
    private final Integer merchantId = 2;

    @Test
    @DisplayName("购物车操作 - 商品不存在")
    void handleCart_goodsNotFound() {
        when(userGoodsMapper.selectById(any(Goods.class))).thenReturn(null);
        CartActionRequest request = new CartActionRequest();
        request.setGoodId(999);
        request.setNum(1);
        BusinessException ex = assertThrows(BusinessException.class, () -> userCartService.handleCart(request, userId));
        assertEquals("商品不存在", ex.getMessage());
    }

    @Test
    @DisplayName("购物车操作 - 不能购买自己的商品")
    void handleCart_cannotBuyOwnGoods() {
        Goods goods = new Goods();
        goods.setId(1);
        goods.setUserId(userId);
        when(userGoodsMapper.selectById(any(Goods.class))).thenReturn(goods);

        CartActionRequest request = new CartActionRequest();
        request.setGoodId(1);
        request.setNum(1);
        BusinessException ex = assertThrows(BusinessException.class, () -> userCartService.handleCart(request, userId));
        assertEquals("不能购买自己的商品", ex.getMessage());
    }

    @Test
    @DisplayName("购物车操作 - 商品已下架")
    void handleCart_goodsOffShelf() {
        Goods goods = new Goods();
        goods.setId(1);
        goods.setUserId(merchantId);
        goods.setLaunch(false);
        when(userGoodsMapper.selectById(any(Goods.class))).thenReturn(goods);
        when(userCartMapper.selectById(any(Cart.class))).thenReturn(null);

        CartActionRequest request = new CartActionRequest();
        request.setGoodId(1);
        request.setNum(1);
        BusinessException ex = assertThrows(BusinessException.class, () -> userCartService.handleCart(request, userId));
        assertEquals("商品已下架", ex.getMessage());
    }

    @Test
    @DisplayName("购物车操作 - 修改已有购物车项数量")
    void handleCart_updateExistingCart() {
        Goods goods = new Goods();
        goods.setId(1);
        goods.setUserId(merchantId);
        goods.setLaunch(true);
        when(userGoodsMapper.selectById(any(Goods.class))).thenReturn(goods);

        Cart existCart = new Cart();
        existCart.setId(10);
        existCart.setUserId(userId);
        existCart.setGoodId(1);
        existCart.setQuantity(1);
        when(userCartMapper.selectById(any(Cart.class))).thenReturn(existCart);

        CartActionRequest request = new CartActionRequest();
        request.setGoodId(1);
        request.setNum(3);
        Result<String> result = userCartService.handleCart(request, userId);
        assertEquals(200, result.getCode());
        assertEquals("修改成功", result.getData());
        verify(userCartMapper).update(existCart);
    }

    @Test
    @DisplayName("购物车操作 - 添加新购物车项成功")
    void handleCart_addNewCartSuccess() {
        Goods goods = new Goods();
        goods.setId(1);
        goods.setUserId(merchantId);
        goods.setLaunch(true);
        when(userGoodsMapper.selectById(any(Goods.class))).thenReturn(goods);
        when(userCartMapper.selectById(any(Cart.class))).thenReturn(null);

        User user = new User();
        user.setId(userId);
        user.setLevel(0);
        when(userUserMapper.selectById(any(User.class))).thenReturn(user);
        when(vipConfigCache.getMaxCartQuantity(0)).thenReturn(10);
        when(userCartMapper.selectList(any(Cart.class))).thenReturn(Collections.emptyList());

        CartActionRequest request = new CartActionRequest();
        request.setGoodId(1);
        request.setNum(1);
        Result<String> result = userCartService.handleCart(request, userId);
        assertEquals(200, result.getCode());
        assertEquals("添加成功", result.getData());
        verify(userCartMapper).insert(any(Cart.class));
    }

    @Test
    @DisplayName("购物车操作 - 购物车数量已达上限")
    void handleCart_cartFull() {
        Goods goods = new Goods();
        goods.setId(1);
        goods.setUserId(merchantId);
        goods.setLaunch(true);
        when(userGoodsMapper.selectById(any(Goods.class))).thenReturn(goods);
        when(userCartMapper.selectById(any(Cart.class))).thenReturn(null);

        User user = new User();
        user.setId(userId);
        user.setLevel(0);
        when(userUserMapper.selectById(any(User.class))).thenReturn(user);
        when(vipConfigCache.getMaxCartQuantity(0)).thenReturn(2);
        when(userCartMapper.selectList(any(Cart.class))).thenReturn(List.of(new Cart(), new Cart()));

        CartActionRequest request = new CartActionRequest();
        request.setGoodId(1);
        request.setNum(1);
        BusinessException ex = assertThrows(BusinessException.class, () -> userCartService.handleCart(request, userId));
        assertTrue(ex.getMessage().contains("已达上限"));
    }

    @Test
    @DisplayName("获取购物车数量")
    void getCartCount() {
        when(userCartMapper.countByUserId(userId)).thenReturn(5);
        Result<Integer> result = userCartService.getCartCount(userId);
        assertEquals(200, result.getCode());
        assertEquals(5, result.getData());
    }

    @Test
    @DisplayName("从购物车购买 - 购物车为空")
    void buyFromCart_emptyCart() {
        BusinessException ex = assertThrows(BusinessException.class, () -> userCartService.buyFromCart(Collections.emptyList(), userId));
        assertEquals("购物车为空", ex.getMessage());
    }

    @Test
    @DisplayName("从购物车购买 - 购物车项不属于当前用户")
    void buyFromCart_cartNotBelongToUser() {
        Cart otherCart = new Cart();
        otherCart.setId(1);
        otherCart.setUserId(999);
        otherCart.setGoodId(10);
        otherCart.setQuantity(1);
        when(userCartMapper.selectByIds(anyList())).thenReturn(List.of(otherCart));

        BusinessException ex = assertThrows(BusinessException.class, () -> userCartService.buyFromCart(List.of(1), userId));
        assertEquals("购物车项不属于当前用户", ex.getMessage());
    }
}