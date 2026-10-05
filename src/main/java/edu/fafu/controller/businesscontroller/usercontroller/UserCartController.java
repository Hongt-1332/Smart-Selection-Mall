package edu.fafu.controller.businesscontroller.usercontroller;
import jakarta.validation.Valid;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import edu.fafu.database.entity.Result;
import edu.fafu.database.dto.request.user.CartActionRequest;
import edu.fafu.service.service.businessservice.userservice.UserCartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@SaCheckLogin
@RestController
@RequestMapping("/user/cart")
public class UserCartController {

    @Autowired
    private UserCartService userCartService;

    @PostMapping
    public Result<String> handleCart(@Valid @RequestBody CartActionRequest request) {
        return userCartService.handleCart(request, StpUtil.getLoginIdAsInt());
    }

    @PostMapping("/buy")
    public Result<String> buyFromCart(@RequestBody List<Integer> cartIds) {
        return userCartService.buyFromCart(cartIds, StpUtil.getLoginIdAsInt());
    }

    @GetMapping("/count")
    public Result<Integer> getCartCount() {
        return userCartService.getCartCount(StpUtil.getLoginIdAsInt());
    }
}