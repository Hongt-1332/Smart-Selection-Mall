package edu.fafu.config;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import edu.fafu.database.entity.VipConfig;
import edu.fafu.database.mapper.businessmapper.vipmapper.VipConfigMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
public class VipConfigCache {

    private static final Logger log = LoggerFactory.getLogger(VipConfigCache.class);
    private static final String CACHE_KEY = "vip:config:all";
    private static final long CACHE_TTL_MINUTES = 10;

    private final VipConfigMapper vipConfigMapper;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public VipConfigCache(VipConfigMapper vipConfigMapper, StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.vipConfigMapper = vipConfigMapper;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void init() {
        refresh();
    }

    @Scheduled(fixedRate = 5 * 60 * 1000)
    public void refresh() {
        List<VipConfig> all = vipConfigMapper.selectAll();
        try {
            redisTemplate.opsForValue().set(CACHE_KEY, objectMapper.writeValueAsString(all), CACHE_TTL_MINUTES, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.warn("VIP配置缓存刷新失败，Redis可能未启动：{}", e.getMessage());
        }
    }

    private List<VipConfig> getAll() {
        try {
            String json = redisTemplate.opsForValue().get(CACHE_KEY);
            if (json == null) {
                refresh();
                json = redisTemplate.opsForValue().get(CACHE_KEY);
            }
            if (json == null) return Collections.emptyList();
            return objectMapper.readValue(json, new TypeReference<List<VipConfig>>() {});
        } catch (Exception e) {
            log.warn("VIP配置缓存读取失败，Redis可能未启动：{}", e.getMessage());
            return Collections.emptyList();
        }
    }

    private VipConfig getConfig(int level) {
        List<VipConfig> all = getAll();
        VipConfig found = null;
        VipConfig defaultConfig = null;
        for (VipConfig c : all) {
            if (c.getLevel() == level) found = c;
            if (c.getLevel() == 0) defaultConfig = c;
        }
        return found != null ? found : defaultConfig;
    }

    public int getMonthlyUpdateGoods(int level) {
        VipConfig c = getConfig(level);
        return c != null ? c.getMonthlyUpdateGoods() : 100;
    }

    public int getMonthlyUpdateAvatar(int level) {
        VipConfig c = getConfig(level);
        return c != null ? c.getMonthlyUpdateAvatar() : 7;
    }

    public int getMaxCartQuantity(int level) {
        VipConfig c = getConfig(level);
        return c != null ? c.getMaxCartQuantity() : 5;
    }

    public int getMaxGoodsQuantity(int level) {
        VipConfig c = getConfig(level);
        return c != null ? c.getMaxGoodsQuantity() : 5;
    }

    public int getMonthlyUpdateBackground(int level) {
        VipConfig c = getConfig(level);
        return c != null && c.getMonthlyUpdateBackground() != null ? c.getMonthlyUpdateBackground() : 5;
    }

    public int getMaxAvatarUpdates() {
        return getMonthlyUpdateAvatar(0);
    }

    public List<VipConfig> getAllConfigs() {
        return getAll();
    }

    public Map<Integer, VipConfig> getConfigMap() {
        List<VipConfig> all = getAll();
        Map<Integer, VipConfig> map = new java.util.HashMap<>();
        for (VipConfig c : all) {
            map.put(c.getLevel(), c);
        }
        return map;
    }
}