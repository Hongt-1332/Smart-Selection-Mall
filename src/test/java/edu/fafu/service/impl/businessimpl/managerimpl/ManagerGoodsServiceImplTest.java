package edu.fafu.service.impl.businessimpl.managerimpl;

import edu.fafu.database.dto.request.manager.UpdateManagerGoodsRequest;
import edu.fafu.database.entity.Goods;
import edu.fafu.database.entity.Result;
import edu.fafu.database.mapper.businessmapper.managermapper.ManagerGoodsMapper;
import edu.fafu.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ManagerGoodsService 单元测试")
class ManagerGoodsServiceImplTest {

    @Mock
    private ManagerGoodsMapper managerGoodsMapper;

    @InjectMocks
    private ManagerGoodsServiceImpl managerGoodsService;

    @Nested
    @DisplayName("删除与下架同步")
    class DeleteLaunchSync {

        @Test
        @DisplayName("删除商品时 launch 必须同步设为 false")
        void deleteGoods_syncsLaunchToFalse() {
            Goods current = new Goods();
            current.setId(1);
            current.setUserId(10);
            current.setGoodsId("G001");
            current.setAddressId(1);
            current.setGoodsName("测试商品");
            current.setDescribe("描述");
            current.setGoodsPrice(java.math.BigDecimal.TEN);
            current.setGoodsStock(100);
            current.setImagePath("/img.png");
            current.setLaunch(true);
            current.setDelete(false);
            when(managerGoodsMapper.selectById(1)).thenReturn(current);
            when(managerGoodsMapper.updateList(anyList())).thenReturn(1);

            UpdateManagerGoodsRequest request = new UpdateManagerGoodsRequest();
            request.setId(1);
            request.setDelete(true);

            Result<String> result = managerGoodsService.updateGoods(request);
            assertEquals(200, result.getCode());

            verify(managerGoodsMapper).updateList(argThat(list -> {
                Goods g = list.get(0);
                return Boolean.TRUE.equals(g.getDelete()) && Boolean.FALSE.equals(g.getLaunch());
            }));
        }

        @Test
        @DisplayName("删除商品时即使显式传 launch=true 也应强制为 false")
        void deleteGoods_forceLaunchFalseEvenIfPassedTrue() {
            Goods current = new Goods();
            current.setId(1);
            current.setUserId(10);
            current.setGoodsId("G001");
            current.setAddressId(1);
            current.setGoodsName("测试商品");
            current.setDescribe("描述");
            current.setGoodsPrice(java.math.BigDecimal.TEN);
            current.setGoodsStock(100);
            current.setImagePath("/img.png");
            current.setLaunch(true);
            current.setDelete(false);
            when(managerGoodsMapper.selectById(1)).thenReturn(current);
            when(managerGoodsMapper.updateList(anyList())).thenReturn(1);

            UpdateManagerGoodsRequest request = new UpdateManagerGoodsRequest();
            request.setId(1);
            request.setDelete(true);
            request.setLaunch(true);

            Result<String> result = managerGoodsService.updateGoods(request);
            assertEquals(200, result.getCode());

            verify(managerGoodsMapper).updateList(argThat(list -> {
                Goods g = list.get(0);
                return Boolean.TRUE.equals(g.getDelete()) && Boolean.FALSE.equals(g.getLaunch());
            }));
        }

        @Test
        @DisplayName("解封商品(delete=false)时 launch 保持原值")
        void undeleteGoods_keepsOriginalLaunch() {
            Goods current = new Goods();
            current.setId(1);
            current.setUserId(10);
            current.setGoodsId("G001");
            current.setAddressId(1);
            current.setGoodsName("测试商品");
            current.setDescribe("描述");
            current.setGoodsPrice(java.math.BigDecimal.TEN);
            current.setGoodsStock(100);
            current.setImagePath("/img.png");
            current.setLaunch(false);
            current.setDelete(true);
            when(managerGoodsMapper.selectById(1)).thenReturn(current);
            when(managerGoodsMapper.updateList(anyList())).thenReturn(1);

            UpdateManagerGoodsRequest request = new UpdateManagerGoodsRequest();
            request.setId(1);
            request.setDelete(false);

            Result<String> result = managerGoodsService.updateGoods(request);
            assertEquals(200, result.getCode());

            verify(managerGoodsMapper).updateList(argThat(list -> {
                Goods g = list.get(0);
                return Boolean.FALSE.equals(g.getDelete()) && Boolean.FALSE.equals(g.getLaunch());
            }));
        }

        @Test
        @DisplayName("解封商品时可以同时设置 launch=true 重新上架")
        void undeleteGoods_canSetLaunchTrue() {
            Goods current = new Goods();
            current.setId(1);
            current.setUserId(10);
            current.setGoodsId("G001");
            current.setAddressId(1);
            current.setGoodsName("测试商品");
            current.setDescribe("描述");
            current.setGoodsPrice(java.math.BigDecimal.TEN);
            current.setGoodsStock(100);
            current.setImagePath("/img.png");
            current.setLaunch(false);
            current.setDelete(true);
            when(managerGoodsMapper.selectById(1)).thenReturn(current);
            when(managerGoodsMapper.updateList(anyList())).thenReturn(1);

            UpdateManagerGoodsRequest request = new UpdateManagerGoodsRequest();
            request.setId(1);
            request.setDelete(false);
            request.setLaunch(true);

            Result<String> result = managerGoodsService.updateGoods(request);
            assertEquals(200, result.getCode());

            verify(managerGoodsMapper).updateList(argThat(list -> {
                Goods g = list.get(0);
                return Boolean.FALSE.equals(g.getDelete()) && Boolean.TRUE.equals(g.getLaunch());
            }));
        }

        @Test
        @DisplayName("商品不存在时返回错误")
        void updateGoods_notFound() {
            when(managerGoodsMapper.selectById(999)).thenReturn(null);

            UpdateManagerGoodsRequest request = new UpdateManagerGoodsRequest();
            request.setId(999);
            request.setDelete(true);

            BusinessException ex = assertThrows(BusinessException.class, () -> managerGoodsService.updateGoods(request));
            assertEquals("商品不存在", ex.getMessage());
        }

        @Test
        @DisplayName("仅修改名称不改变 launch 和 delete")
        void updateGoods_onlyName_unchangedLaunchDelete() {
            Goods current = new Goods();
            current.setId(1);
            current.setUserId(10);
            current.setGoodsId("G001");
            current.setAddressId(1);
            current.setGoodsName("旧名称");
            current.setDescribe("描述");
            current.setGoodsPrice(java.math.BigDecimal.TEN);
            current.setGoodsStock(100);
            current.setImagePath("/img.png");
            current.setLaunch(true);
            current.setDelete(false);
            when(managerGoodsMapper.selectById(1)).thenReturn(current);
            when(managerGoodsMapper.updateList(anyList())).thenReturn(1);

            UpdateManagerGoodsRequest request = new UpdateManagerGoodsRequest();
            request.setId(1);
            request.setGoodsName("新名称");

            Result<String> result = managerGoodsService.updateGoods(request);
            assertEquals(200, result.getCode());

            verify(managerGoodsMapper).updateList(argThat(list -> {
                Goods g = list.get(0);
                return "新名称".equals(g.getGoodsName())
                        && Boolean.TRUE.equals(g.getLaunch())
                        && Boolean.FALSE.equals(g.getDelete());
            }));
        }
    }
}