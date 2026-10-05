package edu.fafu.service.impl.businessimpl.vipimpl;

import tools.jackson.databind.ObjectMapper;
import edu.fafu.config.VipConfigCache;
import edu.fafu.database.entity.Result;
import edu.fafu.database.entity.User;
import edu.fafu.database.entity.VipConfig;
import edu.fafu.database.dto.request.vip.VipBuyRequest;
import edu.fafu.database.dto.response.vip.VipLevelResponse;
import edu.fafu.database.mapper.businessmapper.vipmapper.VipGoodsMapper;
import edu.fafu.database.mapper.businessmapper.vipmapper.VipTradeMapper;
import edu.fafu.database.mapper.businessmapper.vipmapper.VipUserMapper;
import edu.fafu.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.math.BigDecimal;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("VipService 单元测试")
class VipServiceImplTest {

    @Mock
    private VipConfigCache vipConfigCache;
    @Mock
    private VipGoodsMapper vipGoodsMapper;
    @Mock
    private VipUserMapper vipUserMapper;
    @Mock
    private VipTradeMapper vipTradeMapper;
    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;
    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private VipServiceImpl vipService;

    private final Integer userId = 1;

    @Test
    @DisplayName("VIP等级查询 - 用户不存在")
    void vipLevel_userNotFound() {
        when(vipUserMapper.selectById(any(User.class))).thenReturn(null);
        BusinessException ex = assertThrows(BusinessException.class, () -> vipService.vipLevel(userId, null));
        assertEquals("用户不存在", ex.getMessage());
    }

    @Test
    @DisplayName("VIP等级查询 - 指定等级参数时直接返回")
    void vipLevel_withLevelParam() {
        ConcurrentHashMap<Integer, VipConfig> configMap = new ConcurrentHashMap<>();
        configMap.put(1, new VipConfig());
        when(vipConfigCache.getConfigMap()).thenReturn(configMap);

        Result<VipLevelResponse> result = vipService.vipLevel(userId, 1);
        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
    }

    @Test
    @DisplayName("购买VIP - 无效等级")
    void buyVip_invalidLevel() {
        VipBuyRequest request = new VipBuyRequest();
        request.setLevel(null);
        request.setMoney(new BigDecimal("100"));
        BusinessException ex = assertThrows(BusinessException.class, () -> vipService.buyVip(request, userId));
        assertEquals("无效的VIP等级", ex.getMessage());
    }

    @Test
    @DisplayName("购买VIP - 等级配置不存在")
    void buyVip_configNotFound() {
        VipBuyRequest request = new VipBuyRequest();
        request.setLevel(99);
        request.setMoney(new BigDecimal("100"));
        when(vipConfigCache.getConfigMap()).thenReturn(new ConcurrentHashMap<>());
        BusinessException ex = assertThrows(BusinessException.class, () -> vipService.buyVip(request, userId));
        assertEquals("该等级VIP配置不存在", ex.getMessage());
    }

    @Test
    @DisplayName("购买VIP - 金额必须大于0")
    void buyVip_moneyZero() {
        VipBuyRequest request = new VipBuyRequest();
        request.setLevel(1);
        request.setMoney(BigDecimal.ZERO);
        VipConfig config = new VipConfig();
        config.setLevel(1);
        ConcurrentHashMap<Integer, VipConfig> configMap = new ConcurrentHashMap<>();
        configMap.put(1, config);
        when(vipConfigCache.getConfigMap()).thenReturn(configMap);
        BusinessException ex = assertThrows(BusinessException.class, () -> vipService.buyVip(request, userId));
        assertEquals("金额必须大于0", ex.getMessage());
    }

    @Test
    @DisplayName("购买VIP - 用户不存在")
    void buyVip_userNotFound() {
        VipBuyRequest request = new VipBuyRequest();
        request.setLevel(1);
        request.setMoney(new BigDecimal("100"));
        VipConfig config = new VipConfig();
        config.setLevel(1);
        ConcurrentHashMap<Integer, VipConfig> configMap = new ConcurrentHashMap<>();
        configMap.put(1, config);
        when(vipConfigCache.getConfigMap()).thenReturn(configMap);
        when(vipUserMapper.selectById(any(User.class))).thenReturn(null);
        BusinessException ex = assertThrows(BusinessException.class, () -> vipService.buyVip(request, userId));
        assertEquals("用户不存在", ex.getMessage());
    }

    @Test
    @DisplayName("购买VIP - 当前等级已大于等于目标等级")
    void buyVip_levelAlreadyHigher() {
        VipBuyRequest request = new VipBuyRequest();
        request.setLevel(1);
        request.setMoney(new BigDecimal("100"));
        VipConfig config = new VipConfig();
        config.setLevel(1);
        ConcurrentHashMap<Integer, VipConfig> configMap = new ConcurrentHashMap<>();
        configMap.put(1, config);
        when(vipConfigCache.getConfigMap()).thenReturn(configMap);

        User user = new User();
        user.setId(userId);
        user.setLevel(2);
        when(vipUserMapper.selectById(any(User.class))).thenReturn(user);
        BusinessException ex = assertThrows(BusinessException.class, () -> vipService.buyVip(request, userId));
        assertEquals("当前等级已大于等于目标等级", ex.getMessage());
    }

    @Test
    @DisplayName("购买VIP - 余额不足")
    void buyVip_balanceNotEnough() {
        VipBuyRequest request = new VipBuyRequest();
        request.setLevel(1);
        request.setMoney(new BigDecimal("500"));
        VipConfig config = new VipConfig();
        config.setLevel(1);
        config.setVipDuration(30);
        ConcurrentHashMap<Integer, VipConfig> configMap = new ConcurrentHashMap<>();
        configMap.put(1, config);
        when(vipConfigCache.getConfigMap()).thenReturn(configMap);

        User user = new User();
        user.setId(userId);
        user.setLevel(0);
        user.setBalance(new BigDecimal("100"));
        when(vipUserMapper.selectById(any(User.class))).thenReturn(user);
        BusinessException ex = assertThrows(BusinessException.class, () -> vipService.buyVip(request, userId));
        assertEquals("余额不足", ex.getMessage());
    }

    @Test
    @DisplayName("购买VIP - 成功购买")
    void buyVip_success() {
        VipBuyRequest request = new VipBuyRequest();
        request.setLevel(1);
        request.setMoney(new BigDecimal("100"));
        request.setNum(1);
        VipConfig config = new VipConfig();
        config.setLevel(1);
        config.setVipDuration(30);
        ConcurrentHashMap<Integer, VipConfig> configMap = new ConcurrentHashMap<>();
        configMap.put(1, config);
        when(vipConfigCache.getConfigMap()).thenReturn(configMap);

        User user = new User();
        user.setId(userId);
        user.setLevel(0);
        user.setBalance(new BigDecimal("500"));
        when(vipUserMapper.selectById(any(User.class))).thenReturn(user);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        Result<String> result = vipService.buyVip(request, userId);
        assertEquals(200, result.getCode());
        assertEquals("购买成功", result.getData());
        verify(vipUserMapper).updateList(anyList());
    }
}