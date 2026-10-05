package edu.fafu.service.impl.businessimpl.managerimpl;

import cn.dev33.satoken.stp.StpUtil;
import edu.fafu.database.entity.Goods;
import edu.fafu.database.entity.Result;
import edu.fafu.database.entity.User;
import edu.fafu.database.mapper.businessmapper.managermapper.ManagerUserMapper;
import edu.fafu.database.mapper.businessmapper.merchantmapper.MerchantGoodsMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ManagerUserService 单元测试")
class ManagerUserServiceImplTest {

    @Mock
    private ManagerUserMapper managerUserMapper;
    @Mock
    private MerchantGoodsMapper merchantGoodsMapper;
    @Mock
    private StringRedisTemplate redisTemplate;

    @InjectMocks
    private ManagerUserServiceImpl managerUserService;

    private Goods createGoodsQuery(Integer userId) {
        Goods q = new Goods();
        q.setUserId(userId);
        q.setDelete(false);
        q.setLaunch(true);
        return q;
    }

    @Nested
    @DisplayName("封禁用户同步下架商品")
    class BanUserSyncGoods {

        @Test
        @DisplayName("封禁用户时该用户所有上架商品必须下架")
        void banUser_syncsAllOnlineGoodsOffline() {
            Goods g1 = new Goods();
            g1.setId(1);
            g1.setUserId(10);
            g1.setLaunch(true);
            g1.setDelete(false);

            Goods g2 = new Goods();
            g2.setId(2);
            g2.setUserId(10);
            g2.setLaunch(true);
            g2.setDelete(false);

            Goods query = createGoodsQuery(10);
            when(merchantGoodsMapper.selectList(query)).thenReturn(List.of(g1, g2));
            when(managerUserMapper.update(any(User.class))).thenReturn(1);

            User user = new User();
            user.setId(10);
            user.setDelete(true);

            try (MockedStatic<StpUtil> stpUtilMock = mockStatic(StpUtil.class)) {
                Result<String> result = managerUserService.updateUser(user);
                assertEquals(200, result.getCode());

                verify(merchantGoodsMapper).selectList(query);
                verify(merchantGoodsMapper).updateList(argThat(list -> {
                    if (list.size() != 2) return false;
                    return list.stream().allMatch(g -> Boolean.FALSE.equals(g.getLaunch()));
                }));
                stpUtilMock.verify(() -> StpUtil.kickout(10));
            }
        }

        @Test
        @DisplayName("封禁用户时如果无上架商品则不执行更新")
        void banUser_noOnlineGoods_noUpdate() {
            Goods query = createGoodsQuery(10);
            when(merchantGoodsMapper.selectList(query)).thenReturn(Collections.emptyList());
            when(managerUserMapper.update(any(User.class))).thenReturn(1);

            User user = new User();
            user.setId(10);
            user.setDelete(true);

            try (MockedStatic<StpUtil> stpUtilMock = mockStatic(StpUtil.class)) {
                Result<String> result = managerUserService.updateUser(user);
                assertEquals(200, result.getCode());

                verify(merchantGoodsMapper).selectList(query);
                verify(merchantGoodsMapper, never()).updateList(anyList());
            }
        }

        @Test
        @DisplayName("解封用户时不会自动上架商品")
        void unbanUser_doesNotAutoLaunchGoods() {
            when(managerUserMapper.update(any(User.class))).thenReturn(1);

            User user = new User();
            user.setId(10);
            user.setDelete(false);

            Result<String> result = managerUserService.updateUser(user);
            assertEquals(200, result.getCode());

            verify(merchantGoodsMapper, never()).selectList(createGoodsQuery(10));
            verify(merchantGoodsMapper, never()).updateList(anyList());
        }

        @Test
        @DisplayName("修改用户其他信息(delete=null)不影响商品")
        void updateUser_otherFields_noGoodsImpact() {
            when(managerUserMapper.update(any(User.class))).thenReturn(1);

            User user = new User();
            user.setId(10);
            user.setUserName("新名字");

            Result<String> result = managerUserService.updateUser(user);
            assertEquals(200, result.getCode());

            verify(merchantGoodsMapper, never()).selectList(createGoodsQuery(10));
        }
    }

    @Nested
    @DisplayName("封禁用户清除 Session")
    class BanUserClearSession {

        @Test
        @DisplayName("封禁用户时调用StpUtil.kickout清除Session")
        void banUser_clearsSession() {
            Goods query = createGoodsQuery(10);
            when(merchantGoodsMapper.selectList(query)).thenReturn(Collections.emptyList());
            when(managerUserMapper.update(any(User.class))).thenReturn(1);

            User user = new User();
            user.setId(10);
            user.setDelete(true);

            try (MockedStatic<StpUtil> stpUtilMock = mockStatic(StpUtil.class)) {
                Result<String> result = managerUserService.updateUser(user);
                assertEquals(200, result.getCode());

                stpUtilMock.verify(() -> StpUtil.kickout(10));
            }
        }

        @Test
        @DisplayName("解封用户时不调用StpUtil.kickout")
        void unbanUser_doesNotClearSession() {
            when(managerUserMapper.update(any(User.class))).thenReturn(1);

            User user = new User();
            user.setId(10);
            user.setDelete(false);

            try (MockedStatic<StpUtil> stpUtilMock = mockStatic(StpUtil.class)) {
                Result<String> result = managerUserService.updateUser(user);
                assertEquals(200, result.getCode());

                stpUtilMock.verify(() -> StpUtil.kickout(anyInt()), times(0));
            }
        }

        @Test
        @DisplayName("修改用户其他信息时不调用StpUtil.kickout")
        void updateUser_otherFields_doesNotClearSession() {
            when(managerUserMapper.update(any(User.class))).thenReturn(1);

            User user = new User();
            user.setId(10);
            user.setUserName("新名字");

            try (MockedStatic<StpUtil> stpUtilMock = mockStatic(StpUtil.class)) {
                Result<String> result = managerUserService.updateUser(user);
                assertEquals(200, result.getCode());

                stpUtilMock.verify(() -> StpUtil.kickout(anyInt()), times(0));
            }
        }
    }
}