package edu.fafu.service.impl.businessimpl.merchantimpl;

import edu.fafu.config.VipConfigCache;
import edu.fafu.database.entity.Ai;
import edu.fafu.database.entity.Result;
import edu.fafu.database.entity.User;
import edu.fafu.database.dto.request.merchant.AddAiRequest;
import edu.fafu.database.dto.request.merchant.UpdateAiRequest;
import edu.fafu.database.mapper.businessmapper.merchantmapper.AiMapper;
import edu.fafu.database.mapper.businessmapper.merchantmapper.MerchantGoodsMapper;
import edu.fafu.database.mapper.businessmapper.usermapper.UserUserMapper;
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
@DisplayName("MerchantAiService 单元测试")
class MerchantAiServiceImplTest {

    @Mock
    private AiMapper aiMapper;
    @Mock
    private MerchantGoodsMapper merchantGoodsMapper;
    @Mock
    private UserUserMapper userUserMapper;
    @Mock
    private VipConfigCache vipConfigCache;

    @InjectMocks
    private MerchantAiServiceImpl merchantAiService;

    private final Integer userId = 1;

    @Test
    @DisplayName("添加AI商品 - 用户不存在")
    void addAi_userNotFound() {
        when(userUserMapper.selectById(any(User.class))).thenReturn(null);

        AddAiRequest request = new AddAiRequest();
        request.setCategory("家用电器");
        request.setKind("扫地机器人");
        request.setName("科沃斯X2");
        request.setPrice("4599元");
        request.setSimpleDescription("大吸力扫地机");
        request.setFeatures("8000Pa吸力");

        BusinessException ex = assertThrows(BusinessException.class, () -> merchantAiService.addAi(request, userId));
        assertEquals("用户不存在", ex.getMessage());
    }

    @Test
    @DisplayName("添加AI商品 - 成功添加")
    void addAi_success() {
        User user = new User();
        user.setId(userId);
        user.setLevel(0);
        when(userUserMapper.selectById(any(User.class))).thenReturn(user);

        AddAiRequest request = new AddAiRequest();
        request.setCategory("家用电器");
        request.setKind("扫地机器人");
        request.setName("科沃斯X2");
        request.setPrice("4599元");
        request.setSimpleDescription("大吸力扫地机");
        request.setFeatures("8000Pa吸力");

        Result<String> result = merchantAiService.addAi(request, userId);
        assertEquals(200, result.getCode());
        assertEquals("添加成功", result.getData());
        verify(aiMapper).insert(any(Ai.class));
    }

    @Test
    @DisplayName("更新AI商品 - 不存在")
    void updateAi_notFound() {
        UpdateAiRequest request = new UpdateAiRequest();
        request.setId(999);
        when(aiMapper.selectById(999)).thenReturn(null);
        BusinessException ex = assertThrows(BusinessException.class, () -> merchantAiService.updateAi(request, userId));
        assertEquals("AI不存在", ex.getMessage());
    }

    @Test
    @DisplayName("更新AI商品 - 不属于当前商户")
    void updateAi_notBelongToUser() {
        Ai old = new Ai();
        old.setId(1);
        old.setUserId(999);
        when(aiMapper.selectById(1)).thenReturn(old);

        UpdateAiRequest request = new UpdateAiRequest();
        request.setId(1);
        BusinessException ex = assertThrows(BusinessException.class, () -> merchantAiService.updateAi(request, userId));
        assertEquals("AI不属于当前用户", ex.getMessage());
    }

    @Test
    @DisplayName("更新AI商品 - 成功更新")
    void updateAi_success() {
        Ai old = new Ai();
        old.setId(1);
        old.setUserId(userId);
        when(aiMapper.selectById(1)).thenReturn(old);

        UpdateAiRequest request = new UpdateAiRequest();
        request.setId(1);
        request.setCategory("新分类");
        request.setKind("新种类");
        request.setName("新名称");
        request.setPrice("新价格");
        request.setSimpleDescription("新描述");
        request.setFeatures("新特性");

        Result<String> result = merchantAiService.updateAi(request, userId);
        assertEquals(200, result.getCode());
        assertEquals("修改成功", result.getData());
        verify(aiMapper).update(any(Ai.class));
    }

    @Test
    @DisplayName("删除AI商品 - 不存在")
    void deleteAi_notFound() {
        when(aiMapper.selectById(999)).thenReturn(null);
        BusinessException ex = assertThrows(BusinessException.class, () -> merchantAiService.deleteAi(999, userId));
        assertEquals("AI不存在", ex.getMessage());
    }

    @Test
    @DisplayName("删除AI商品 - 不属于当前用户")
    void deleteAi_notBelongToUser() {
        Ai old = new Ai();
        old.setId(1);
        old.setUserId(999);
        when(aiMapper.selectById(1)).thenReturn(old);

        BusinessException ex = assertThrows(BusinessException.class, () -> merchantAiService.deleteAi(1, userId));
        assertEquals("AI不属于当前用户", ex.getMessage());
    }

    @Test
    @DisplayName("删除AI商品 - 成功删除")
    void deleteAi_success() {
        Ai old = new Ai();
        old.setId(1);
        old.setUserId(userId);
        when(aiMapper.selectById(1)).thenReturn(old);

        Result<String> result = merchantAiService.deleteAi(1, userId);
        assertEquals(200, result.getCode());
        assertEquals("删除成功", result.getData());
        verify(aiMapper).update(any(Ai.class));
    }
}