package edu.fafu.service.impl.businessimpl.merchantimpl;

import edu.fafu.config.SystemConfig;
import edu.fafu.config.VipConfigCache;
import edu.fafu.database.entity.Address;
import edu.fafu.database.entity.Goods;
import edu.fafu.database.entity.Result;
import edu.fafu.database.entity.User;
import edu.fafu.database.dto.request.merchant.AddMerchantGoodsRequest;
import edu.fafu.database.dto.request.merchant.UpdateMerchantGoodsRequest;
import edu.fafu.database.mapper.businessmapper.merchantmapper.MerchantAddressMapper;
import edu.fafu.database.mapper.businessmapper.merchantmapper.MerchantGoodsMapper;
import edu.fafu.database.mapper.businessmapper.merchantmapper.MerchantUserMapper;
import edu.fafu.database.mapper.businessmapper.usermapper.UserUserMapper;
import edu.fafu.exception.BusinessException;
import edu.fafu.tool.oss.OssUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MerchantGoodsService 单元测试")
class MerchantGoodsServiceImplTest {

    @Mock
    private MerchantGoodsMapper merchantGoodsMapper;
    @Mock
    private MerchantAddressMapper merchantAddressMapper;
    @Mock
    private MerchantUserMapper merchantUserMapper;
    @Mock
    private VipConfigCache vipConfigCache;
    @Mock
    private UserUserMapper userUserMapper;
    @Mock
    private OssUtil ossUtil;
    @Mock
    private SystemConfig systemConfig;

    @InjectMocks
    private MerchantGoodsServiceImpl merchantGoodsService;

    private final Integer userId = 1;

    @Test
    @DisplayName("添加商品 - 地址不存在返回未设置默认地址")
    void addGoods_addressNotExist() {
        AddMerchantGoodsRequest request = new AddMerchantGoodsRequest();
        request.setAddressId(999);
        when(merchantAddressMapper.selectById(any(Address.class))).thenReturn(null);
        BusinessException ex = assertThrows(BusinessException.class, () -> merchantGoodsService.addGoods(request, null, userId));
        assertEquals("未设置默认地址", ex.getMessage());
    }

    @Test
    @DisplayName("添加商品 - 商品上架数量已达上限")
    void addGoods_goodsLimitExceeded() {
        AddMerchantGoodsRequest request = new AddMerchantGoodsRequest();
        request.setAddressId(1);
        request.setGoodsName("测试商品");
        request.setGoodsPrice(new BigDecimal("10.00"));
        request.setGoodsStock(10);
        when(merchantAddressMapper.selectById(any(Address.class))).thenReturn(new Address());

        User user = new User();
        user.setId(userId);
        user.setLevel(0);
        when(merchantUserMapper.selectById(any(User.class))).thenReturn(user);
        when(vipConfigCache.getMaxGoodsQuantity(0)).thenReturn(2);
        when(merchantGoodsMapper.selectList(any(Goods.class))).thenReturn(List.of(new Goods(), new Goods()));

        BusinessException ex = assertThrows(BusinessException.class, () -> merchantGoodsService.addGoods(request, null, userId));
        assertTrue(ex.getMessage().contains("已达上限"));
    }

    @Test
    @DisplayName("添加商品 - 商品名称已存在")
    void addGoods_goodsNameAlreadyExists() {
        AddMerchantGoodsRequest request = new AddMerchantGoodsRequest();
        request.setAddressId(1);
        request.setGoodsName("重复商品");
        request.setGoodsPrice(new BigDecimal("10.00"));
        request.setGoodsStock(10);
        when(merchantAddressMapper.selectById(any(Address.class))).thenReturn(new Address());

        User user = new User();
        user.setId(userId);
        user.setLevel(0);
        user.setUploadGoods(0);
        user.setUploadTime(java.time.LocalDateTime.now());
        when(merchantUserMapper.selectById(any(User.class))).thenReturn(user);
        when(vipConfigCache.getMaxGoodsQuantity(0)).thenReturn(10);
        when(vipConfigCache.getMonthlyUpdateGoods(0)).thenReturn(10);
        when(merchantGoodsMapper.selectList(any(Goods.class))).thenReturn(List.of(new Goods()));

        BusinessException ex = assertThrows(BusinessException.class, () -> merchantGoodsService.addGoods(request, null, userId));
        assertEquals("商品名称已存在", ex.getMessage());
    }

    @Test
    @DisplayName("添加商品 - 价格低于最低限制")
    void addGoods_priceTooLow() {
        AddMerchantGoodsRequest request = new AddMerchantGoodsRequest();
        request.setAddressId(1);
        request.setGoodsName("测试商品");
        request.setGoodsPrice(new BigDecimal("0.001"));
        request.setGoodsStock(10);
        when(merchantAddressMapper.selectById(any(Address.class))).thenReturn(new Address());

        User user = new User();
        user.setId(userId);
        user.setLevel(0);
        user.setUploadGoods(0);
        user.setUploadTime(java.time.LocalDateTime.now());
        when(merchantUserMapper.selectById(any(User.class))).thenReturn(user);
        when(vipConfigCache.getMaxGoodsQuantity(0)).thenReturn(10);
        when(merchantGoodsMapper.selectList(any(Goods.class))).thenReturn(Collections.emptyList());
        when(vipConfigCache.getMonthlyUpdateGoods(0)).thenReturn(10);
        when(systemConfig.getGoodsMinPrice()).thenReturn(new BigDecimal("0.01"));

        BusinessException ex = assertThrows(BusinessException.class, () -> merchantGoodsService.addGoods(request, null, userId));
        assertTrue(ex.getMessage().contains("不能小于"));
    }

    @Test
    @DisplayName("更新商品 - 商品ID为空时selectById返回null")
    void updateGoods_idNull() {
        UpdateMerchantGoodsRequest request = new UpdateMerchantGoodsRequest();
        request.setId(null);
        when(merchantGoodsMapper.selectById(any(Goods.class))).thenReturn(null);
        BusinessException ex = assertThrows(BusinessException.class, () -> merchantGoodsService.updateGoods(request, userId));
        assertEquals("商品不存在", ex.getMessage());
    }

    @Test
    @DisplayName("更新商品 - 商品不存在")
    void updateGoods_notFound() {
        UpdateMerchantGoodsRequest request = new UpdateMerchantGoodsRequest();
        request.setId(999);
        when(merchantGoodsMapper.selectById(any(Goods.class))).thenReturn(null);
        BusinessException ex = assertThrows(BusinessException.class, () -> merchantGoodsService.updateGoods(request, userId));
        assertEquals("商品不存在", ex.getMessage());
    }

    @Test
    @DisplayName("更新商品 - 不属于当前商户")
    void updateGoods_notBelongToUser() {
        Goods old = new Goods();
        old.setId(1);
        old.setUserId(999);
        when(merchantGoodsMapper.selectById(any(Goods.class))).thenReturn(old);

        UpdateMerchantGoodsRequest request = new UpdateMerchantGoodsRequest();
        request.setId(1);
        BusinessException ex = assertThrows(BusinessException.class, () -> merchantGoodsService.updateGoods(request, userId));
        assertEquals("商品不属于当前商户", ex.getMessage());
    }

    @Test
    @DisplayName("更新描述 - 成功更新")
    void updateDescribe_success() {
        Goods old = new Goods();
        old.setId(1);
        old.setUserId(userId);
        when(merchantGoodsMapper.selectById(any(Goods.class))).thenReturn(old);

        Result<String> result = merchantGoodsService.updateDescribe(1, "新描述", userId);
        assertEquals(200, result.getCode());
        assertEquals("修改成功", result.getData());
        verify(merchantGoodsMapper).update(any(Goods.class));
    }

    @Test
    @DisplayName("更新上架状态 - 成功更新")
    void updateLaunch_success() {
        Goods old = new Goods();
        old.setId(1);
        old.setUserId(userId);
        old.setDelete(false);
        when(merchantGoodsMapper.selectById(any(Goods.class))).thenReturn(old);

        Result<String> result = merchantGoodsService.updateLaunch(1, true, userId);
        assertEquals(200, result.getCode());
        assertEquals("修改成功", result.getData());
        verify(merchantGoodsMapper).update(any(Goods.class));
    }

    @Test
    @DisplayName("更新上架状态 - 已删除商品无法上架")
    void updateLaunch_deletedGoodsCannotLaunch() {
        Goods old = new Goods();
        old.setId(1);
        old.setUserId(userId);
        old.setDelete(true);
        when(merchantGoodsMapper.selectById(any(Goods.class))).thenReturn(old);

        BusinessException ex = assertThrows(BusinessException.class, () -> merchantGoodsService.updateLaunch(1, true, userId));
        assertEquals("商品已被删除，无法上架", ex.getMessage());
        verify(merchantGoodsMapper, never()).update(any(Goods.class));
    }
}