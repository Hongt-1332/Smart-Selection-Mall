package edu.fafu.controller.businesscontroller.usercontroller;
import jakarta.validation.Valid;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import edu.fafu.database.entity.Result;
import edu.fafu.database.dto.request.user.LoginRequest;
import edu.fafu.database.dto.request.user.RegisterRequest;
import edu.fafu.database.dto.request.user.WechatLoginRequest;
import edu.fafu.database.dto.request.user.UpdateRequest;
import edu.fafu.database.dto.response.user.UserInfoVO;
import edu.fafu.database.dto.response.user.WechatLoginResponse;
import edu.fafu.service.service.businessservice.userservice.UserUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/user")
public class UserUserController {

    @Autowired
    private UserUserService userUserService;

    @PostMapping("/register")
    public Result<String> register(@Valid @RequestBody RegisterRequest request) {
        return userUserService.register(request);
    }

    @PostMapping("/login")
    public Result<java.util.Map<String, String>> login(@Valid @RequestBody LoginRequest request) {
        return userUserService.login(request);
    }

    @PostMapping("/wechatLogin")
    public Result<WechatLoginResponse> wechatLogin(@Valid @RequestBody WechatLoginRequest request) {
        return userUserService.wechatLogin(request);
    }

    @SaCheckLogin
    @PostMapping("/logout")
    public Result<String> logout() {
        return userUserService.logout(StpUtil.getLoginIdAsInt());
    }

    @SaCheckLogin
    @PutMapping("/update")
    public Result<String> update(@Valid @RequestBody UpdateRequest request) {
        return userUserService.update(request, StpUtil.getLoginIdAsInt());
    }

    @SaCheckLogin
    @PutMapping("/setDefaultAddress")
    public Result<String> setDefaultAddress(@RequestParam("addressId") Integer addressId) {
        return userUserService.setDefaultAddress(addressId, StpUtil.getLoginIdAsInt());
    }

    @SaCheckLogin
    @PostMapping("/updateImage")
    public Result<String> updateUserImage(@RequestParam("file") MultipartFile file) {
        return userUserService.updateUserImage(file, StpUtil.getLoginIdAsInt());
    }

    @SaCheckLogin
    @PutMapping("/updateDescribe")
    public Result<String> updateDescribe(@RequestParam("describe") String describe) {
        return userUserService.updateDescribe(describe, StpUtil.getLoginIdAsInt());
    }

    @SaCheckLogin
    @GetMapping("/info")
    public Result<UserInfoVO> getUserInfo() {
        return userUserService.getUserInfo(StpUtil.getLoginIdAsInt());
    }
}