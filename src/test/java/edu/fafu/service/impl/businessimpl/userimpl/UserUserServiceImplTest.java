package edu.fafu.service.impl.businessimpl.userimpl;

import edu.fafu.config.SystemConfig;
import edu.fafu.config.VipConfigCache;
import edu.fafu.database.entity.Result;
import edu.fafu.database.entity.User;
import edu.fafu.database.dto.request.user.UpdateRequest;
import edu.fafu.database.mapper.businessmapper.usermapper.UserUserMapper;
import edu.fafu.database.mapper.businessmapper.vipmapper.VipConfigMapper;
import edu.fafu.database.mapper.businessmapper.usermapper.BackgroundMapper;
import edu.fafu.exception.BusinessException;
import edu.fafu.tool.oss.OssUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserUserService 单元测试")
class UserUserServiceImplTest {

    @Mock
    private UserUserMapper userUserMapper;
    @Mock
    private VipConfigCache vipConfigCache;
    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private BackgroundMapper backgroundMapper;
    @Mock
    private VipConfigMapper vipConfigMapper;
    @Mock
    private OssUtil ossUtil;
    @Mock
    private SystemConfig systemConfig;

    @InjectMocks
    private UserUserServiceImpl userUserService;

    private final Integer userId = 1;

    @Test
    @DisplayName("更新用户信息 - 用户不存在")
    void update_userNotFound() {
        when(userUserMapper.selectById(any(User.class))).thenReturn(null);
        UpdateRequest request = new UpdateRequest();
        request.setUserName("新名字");
        BusinessException ex = assertThrows(BusinessException.class, () -> userUserService.update(request, userId));
        assertEquals("用户不存在", ex.getMessage());
    }

    @Test
    @DisplayName("更新用户信息 - 用户名已存在")
    void update_userNameAlreadyExists() {
        User existingUser = new User();
        existingUser.setId(userId);
        existingUser.setUserName("旧名字");
        when(userUserMapper.selectById(any(User.class))).thenReturn(existingUser);
        when(userUserMapper.selectList(any(User.class))).thenReturn(List.of(new User()));

        UpdateRequest request = new UpdateRequest();
        request.setUserName("已存在的名字");
        BusinessException ex = assertThrows(BusinessException.class, () -> userUserService.update(request, userId));
        assertEquals("用户名已存在", ex.getMessage());
    }

    @Test
    @DisplayName("更新用户信息 - 成功更新")
    void update_success() {
        User existingUser = new User();
        existingUser.setId(userId);
        existingUser.setUserName("旧名字");
        existingUser.setEmail("old@test.com");
        existingUser.setPhone("13800000000");
        when(userUserMapper.selectById(any(User.class))).thenReturn(existingUser);
        when(userUserMapper.selectList(any(User.class))).thenReturn(Collections.emptyList());

        UpdateRequest request = new UpdateRequest();
        request.setUserName("新名字");
        request.setEmail("new@test.com");
        request.setPhone("13900000000");
        Result<String> result = userUserService.update(request, userId);
        assertEquals(200, result.getCode());
        assertEquals("更新成功", result.getData());
        verify(userUserMapper).updateById(any(User.class));
    }

    @Test
    @DisplayName("设置默认地址 - 用户不存在")
    void setDefaultAddress_userNotFound() {
        when(userUserMapper.selectById(any(User.class))).thenReturn(null);
        BusinessException ex = assertThrows(BusinessException.class, () -> userUserService.setDefaultAddress(1, userId));
        assertEquals("用户不存在", ex.getMessage());
    }

    @Test
    @DisplayName("设置默认地址 - 成功设置")
    void setDefaultAddress_success() {
        User user = new User();
        user.setId(userId);
        when(userUserMapper.selectById(any(User.class))).thenReturn(user);
        Result<String> result = userUserService.setDefaultAddress(5, userId);
        assertEquals(200, result.getCode());
        assertEquals("默认地址设置成功", result.getData());
        verify(userUserMapper).updateById(any(User.class));
    }

    @Test
    @DisplayName("更新描述 - 成功更新")
    void updateDescribe_success() {
        User user = new User();
        user.setId(userId);
        when(userUserMapper.selectById(any(User.class))).thenReturn(user);
        Result<String> result = userUserService.updateDescribe("新描述", userId);
        assertEquals(200, result.getCode());
        assertEquals("更新描述成功", result.getData());
        verify(userUserMapper).updateById(any(User.class));
    }

    @Test
    @DisplayName("获取用户信息 - 用户不存在")
    void getUserInfo_userNotFound() {
        when(userUserMapper.selectById(any(User.class))).thenReturn(null);
        BusinessException ex = assertThrows(BusinessException.class, () -> userUserService.getUserInfo(userId));
        assertEquals("用户不存在", ex.getMessage());
    }
}