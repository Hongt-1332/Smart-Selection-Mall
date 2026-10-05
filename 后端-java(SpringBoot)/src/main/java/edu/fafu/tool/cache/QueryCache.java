package edu.fafu.tool.cache;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class QueryCache {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public QueryCache(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public <T> T getOrLoad(String key, Class<T> clazz, Supplier<T> loader, long ttlMinutes) {
        try {
            String cached = redisTemplate.opsForValue().get(key);
            if (cached != null) {
                return objectMapper.readValue(cached, clazz);
            }
        } catch (Exception ignored) {}
        T value = loader.get();
        if (value != null) {
            try {
                redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(value), ttlMinutes, TimeUnit.MINUTES);
            } catch (Exception ignored) {}
        }
        return value;
    }

    public <T> T getOrLoad(String key, TypeReference<T> typeRef, Supplier<T> loader, long ttlMinutes) {
        try {
            String cached = redisTemplate.opsForValue().get(key);
            if (cached != null) {
                return objectMapper.readValue(cached, typeRef);
            }
        } catch (Exception ignored) {}
        T value = loader.get();
        if (value != null) {
            try {
                redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(value), ttlMinutes, TimeUnit.MINUTES);
            } catch (Exception ignored) {}
        }
        return value;
    }

    public Integer getOrLoadInt(String key, Supplier<Integer> loader, long ttlMinutes) {
        try {
            String cached = redisTemplate.opsForValue().get(key);
            if (cached != null) {
                return Integer.parseInt(cached);
            }
        } catch (Exception ignored) {}
        Integer value = loader.get();
        if (value != null) {
            try {
                redisTemplate.opsForValue().set(key, String.valueOf(value), ttlMinutes, TimeUnit.MINUTES);
            } catch (Exception ignored) {}
        }
        return value;
    }
}