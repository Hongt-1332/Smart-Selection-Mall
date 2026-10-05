package edu.fafu.controller.businesscontroller.managercontroller;

import edu.fafu.database.dto.request.manager.UpdateManagerBackgroundRequest;
import edu.fafu.database.entity.Result;
import edu.fafu.service.service.businessservice.managerservice.ManagerBackgroundService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import cn.dev33.satoken.annotation.SaCheckRole;
import org.springframework.web.bind.annotation.RestController;

@SaCheckRole("admin")
@RestController
@RequestMapping("/manager/background")
public class ManagerBackgroundController {

    @Autowired
    private ManagerBackgroundService managerBackgroundService;

    @PutMapping("/updateBackground")
    public Result<String> updateBackground(@Valid @RequestBody UpdateManagerBackgroundRequest request) {
        return managerBackgroundService.updateBackground(request);
    }
}