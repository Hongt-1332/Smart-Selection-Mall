package edu.fafu.service.impl.businessimpl.vipimpl;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import edu.fafu.config.VipConfigCache;
import edu.fafu.database.dto.response.common.ImageResponse;
import edu.fafu.database.entity.Result;
import edu.fafu.database.mapper.businessmapper.vipmapper.VipGoodsMapper;
import edu.fafu.database.mapper.businessmapper.vipmapper.VipTradeMapper;
import edu.fafu.database.mapper.businessmapper.vipmapper.VipUserMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("VipService Redis缓存测试")
class VipServiceImplCacheTest {

    @Mock
    private VipConfigCache vipConfigCache;
    @Mock
    private VipGoodsMapper vipGoodsMapper;
    @Mock
    private VipUserMapper vipUserMapper;
    @Mock
    private VipTradeMapper vipTradeMapper;
    @Mock(lenient = true)
    private StringRedisTemplate redisTemplate;
    @Mock(lenient = true)
    private ValueOperations<String, String> valueOperations;
    @Mock(lenient = true)
    private ObjectMapper objectMapper;

    @InjectMocks
    private VipServiceImpl vipService;

    @Nested
    @DisplayName("vipImage Redis缓存测试")
    class VipImageCacheTest {

        @Test
        @DisplayName("Redis无缓存时，加载图片并写入Redis")
        void noCache_loadsAndCachesToRedis() throws Exception {
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            when(valueOperations.get(anyString())).thenReturn(null);
            when(objectMapper.writeValueAsString(any())).thenReturn("[]");

            Result<List<ImageResponse>> result = vipService.vipImage();
            assertEquals(200, result.getCode());
            assertNotNull(result.getData());

            verify(valueOperations, atLeastOnce()).get("vip:cache:images");
            verify(valueOperations, atLeastOnce()).set(eq("vip:cache:images"), anyString(), anyLong(), any());
        }

        @Test
        @DisplayName("Redis有缓存时，直接返回缓存")
        void hasCache_returnsCached() throws Exception {
            String cachedJson = "[{\"imageUrl\":\"test.webp\",\"imagePath\":\"/img/test.webp\"}]";
            List<ImageResponse> cachedList = List.of(new ImageResponse("/img/test.webp", "test.webp"));

            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            when(valueOperations.get("vip:cache:images")).thenReturn(cachedJson);
            when(objectMapper.readValue(eq(cachedJson), any(TypeReference.class))).thenReturn(cachedList);

            Result<List<ImageResponse>> result = vipService.vipImage();
            assertEquals(200, result.getCode());
            assertNotNull(result.getData());

            verify(valueOperations).get("vip:cache:images");
            verify(valueOperations, never()).set(eq("vip:cache:images"), anyString(), anyLong(), any());
        }
    }

    @Nested
    @DisplayName("vipHomeImage Redis缓存测试")
    class VipHomeImageCacheTest {

        @Test
        @DisplayName("Redis无缓存时，加载图片并写入Redis")
        void noCache_loadsAndCachesToRedis() throws Exception {
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            when(valueOperations.get(anyString())).thenReturn(null);
            when(objectMapper.writeValueAsString(any())).thenReturn("[]");

            Result<List<ImageResponse>> result = vipService.vipHomeImage();
            assertEquals(200, result.getCode());
            assertNotNull(result.getData());

            verify(valueOperations).get("vip:cache:homeImages");
            verify(valueOperations).set(eq("vip:cache:homeImages"), anyString(), anyLong(), any());
        }
    }
}