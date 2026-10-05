package edu.fafu.controller.businesscontroller.managercontroller;
import edu.fafu.database.entity.Result;
import edu.fafu.database.entity.VipConfig;
import edu.fafu.service.service.businessservice.managerservice.ManagerConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import cn.dev33.satoken.annotation.SaCheckRole;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@SaCheckRole("admin")
@RestController
@RequestMapping("/manager/config")
public class ManagerConfigController {

    @Autowired
    private ManagerConfigService managerConfigService;

    @GetMapping("/user")
    public Result<List<VipConfig>> getUserConfig() {
        return managerConfigService.getAllUserConfig();
    }

    @PostMapping("/user")
    public Result<String> saveUserConfig(@RequestBody VipConfig entity) {
        return managerConfigService.saveUserConfig(entity);
    }

    @GetMapping("/merchant")
    public Result<Map<String, Object>> getMerchantConfig() {
        return managerConfigService.getAllMerchantConfig();
    }

    @PostMapping("/merchant")
    public Result<String> saveMerchantConfig(@RequestBody VipConfig entity) {
        return managerConfigService.saveMerchantConfig(entity);
    }

    @PostMapping("/vip")
    public Result<String> saveVipConfig(@RequestBody VipConfig entity) {
        return managerConfigService.saveVipConfig(entity);
    }

    @PutMapping("/vip")
    public Result<String> updateVipConfig(@RequestBody VipConfig entity) {
        return managerConfigService.updateVipConfig(entity);
    }
}