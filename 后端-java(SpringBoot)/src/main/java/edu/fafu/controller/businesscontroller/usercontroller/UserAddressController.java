package edu.fafu.controller.businesscontroller.usercontroller;
import jakarta.validation.Valid;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import edu.fafu.database.entity.Result;
import edu.fafu.database.dto.request.user.AddAddressRequest;
import edu.fafu.database.dto.request.user.UpdateAddressRequest;
import edu.fafu.database.dto.response.user.UserAddress;
import edu.fafu.service.service.businessservice.userservice.UserAddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@SaCheckLogin
@RestController
@RequestMapping("/user/address")
public class UserAddressController {

    @Autowired
    private UserAddressService userAddressService;

    @GetMapping("/show")
    public Result<List<UserAddress>> showUserAddress() {
        return userAddressService.showUserAddress(StpUtil.getLoginIdAsInt());
    }

    @PostMapping("/add")
    public Result<String> addUserAddress(@Valid @RequestBody AddAddressRequest request) {
        return userAddressService.addUserAddress(request, StpUtil.getLoginIdAsInt());
    }

    @PutMapping("/update")
    public Result<String> updateUserAddress(@Valid @RequestBody UpdateAddressRequest request) {
        return userAddressService.updateUserAddress(request, StpUtil.getLoginIdAsInt());
    }

    @DeleteMapping("/delete")
    public Result<String> deleteUserAddress(@RequestParam("id") Integer id) {
        return userAddressService.deleteUserAddress(id, StpUtil.getLoginIdAsInt());
    }
}