package edu.fafu.service.service.businessservice.managerservice;

import edu.fafu.database.entity.Result;
import edu.fafu.database.entity.VipConfig;

import java.util.List;
import java.util.Map;

public interface ManagerConfigService {

    Result<List<VipConfig>> getAllUserConfig();

    Result<String> saveUserConfig(VipConfig entity);

    Result<Map<String, Object>> getAllMerchantConfig();

    Result<String> saveMerchantConfig(VipConfig entity);

    Result<String> saveVipConfig(VipConfig entity);

    Result<String> updateVipConfig(VipConfig entity);
}