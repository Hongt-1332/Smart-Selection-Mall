package edu.fafu.controller.businesscontroller.usercontroller;
import jakarta.validation.Valid;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import edu.fafu.database.entity.Result;
import edu.fafu.database.dto.request.user.AddBackgroundRequest;
import edu.fafu.database.dto.request.user.PaymentActionRequest;
import edu.fafu.database.dto.request.user.UpdateBackgroundRequest;
import edu.fafu.service.service.businessservice.userservice.UserBackgroundService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@SaCheckLogin
@RestController
@RequestMapping("/user/background")
public class UserBackgroundController {

    @Autowired
    private UserBackgroundService userBackgroundService;

    @PostMapping("/add")
    public Result<String> addBackground(@Valid @RequestBody AddBackgroundRequest request) {
        return userBackgroundService.addBackground(request, StpUtil.getLoginIdAsInt());
    }

    @PutMapping("/update")
    public Result<String> updateBackground(@Valid @RequestBody UpdateBackgroundRequest request) {
        return userBackgroundService.updateBackground(request, StpUtil.getLoginIdAsInt());
    }

    @PostMapping("/pay")
    public Result<String> pay(@Valid @RequestBody PaymentActionRequest request) {
        return userBackgroundService.pay(request, StpUtil.getLoginIdAsInt());
    }
}