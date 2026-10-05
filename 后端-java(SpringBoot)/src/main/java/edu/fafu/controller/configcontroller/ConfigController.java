package edu.fafu.controller.configcontroller;

import tools.jackson.databind.ObjectMapper;
import edu.fafu.config.SystemConfig;
import edu.fafu.config.VipConfigCache;
import edu.fafu.database.entity.Result;
import edu.fafu.database.entity.VipConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import cn.dev33.satoken.annotation.SaCheckRole;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@SaCheckRole("admin")
@RestController
@RequestMapping("/config")
public class ConfigController {

    private static final String CACHE_KEY_USER = "api:config:user";
    private static final String CACHE_KEY_MERCHANT = "api:config:merchant";
    private static final long CACHE_TTL_MINUTES = 5;

    @Autowired
    private VipConfigCache vipConfigCache;
    @Autowired
    private SystemConfig systemConfig;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private ObjectMapper objectMapper;

    @GetMapping("/user")
    public Result<List<VipConfig>> getUserConfig() {
        return Result.success(vipConfigCache.getAllConfigs());
    }

    @GetMapping("/merchant")
    public Result<Map<String, Object>> getMerchantConfig() {
        try {
            String cached = redisTemplate.opsForValue().get(CACHE_KEY_MERCHANT);
            if (cached != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> map = objectMapper.readValue(cached, Map.class);
                return Result.success(map);
            }
        } catch (Exception ignored) {}

        Map<String, Object> data = new HashMap<>();
        data.put("backgroundSequenceCount", systemConfig.getBackgroundSequenceCount());
        data.put("configs", vipConfigCache.getAllConfigs());

        try {
            redisTemplate.opsForValue().set(CACHE_KEY_MERCHANT, objectMapper.writeValueAsString(data), CACHE_TTL_MINUTES, TimeUnit.MINUTES);
        } catch (Exception ignored) {}

        return Result.success(data);
    }
}