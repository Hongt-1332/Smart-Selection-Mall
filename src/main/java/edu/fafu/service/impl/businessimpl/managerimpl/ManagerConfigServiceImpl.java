package edu.fafu.service.impl.businessimpl.managerimpl;

import edu.fafu.config.SystemConfig;
import edu.fafu.config.VipConfigCache;
import edu.fafu.database.entity.Result;
import edu.fafu.database.entity.VipConfig;
import edu.fafu.database.mapper.businessmapper.managermapper.ManagerConfigMapper;
import edu.fafu.service.service.businessservice.managerservice.ManagerConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ManagerConfigServiceImpl implements ManagerConfigService {

    private static final String CACHE_KEY_MERCHANT = "api:config:merchant";

    @Autowired
    private VipConfigCache vipConfigCache;
    @Autowired
    private ManagerConfigMapper managerConfigMapper;
    @Autowired
    private SystemConfig systemConfig;
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public Result<List<VipConfig>> getAllUserConfig() {
        return Result.success(vipConfigCache.getAllConfigs());
    }

    @Override
    public Result<String> saveUserConfig(VipConfig entity) {
        VipConfig existing = managerConfigMapper.selectByLevel(entity.getLevel());
        if (existing != null) {
            managerConfigMapper.update(entity);
        } else {
            managerConfigMapper.insert(entity);
        }
        vipConfigCache.refresh();
        redisTemplate.delete(CACHE_KEY_MERCHANT);
        return Result.success("保存成功");
    }

    @Override
    public Result<Map<String, Object>> getAllMerchantConfig() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("sequenceCount", systemConfig.getBackgroundSequenceCount());
        map.put("levels", vipConfigCache.getAllConfigs());
        return Result.success(map);
    }

    @Override
    public Result<String> saveMerchantConfig(VipConfig entity) {
        VipConfig existing = managerConfigMapper.selectByLevel(entity.getLevel());
        if (existing != null) {
            managerConfigMapper.update(entity);
        } else {
            managerConfigMapper.insert(entity);
        }
        vipConfigCache.refresh();
        redisTemplate.delete(CACHE_KEY_MERCHANT);
        return Result.success("保存成功");
    }

    @Override
    public Result<String> saveVipConfig(VipConfig entity) {
        managerConfigMapper.insert(entity);
        vipConfigCache.refresh();
        redisTemplate.delete(CACHE_KEY_MERCHANT);
        return Result.success("新增成功");
    }

    @Override
    public Result<String> updateVipConfig(VipConfig entity) {
        managerConfigMapper.update(entity);
        vipConfigCache.refresh();
        redisTemplate.delete(CACHE_KEY_MERCHANT);
        return Result.success("修改成功");
    }
}