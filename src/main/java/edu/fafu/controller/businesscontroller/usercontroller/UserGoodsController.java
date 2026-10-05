package edu.fafu.controller.businesscontroller.usercontroller;
import jakarta.validation.Valid;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import edu.fafu.database.entity.Result;
import edu.fafu.database.dto.request.user.BuyGoodsRequest;
import edu.fafu.service.service.businessservice.userservice.UserGoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SaCheckLogin
@RestController
@RequestMapping("/user/goods")
public class UserGoodsController {

    @Autowired
    private UserGoodsService userGoodsService;

    @PostMapping("/buy")
    public Result<String> buyGoods(@Valid @RequestBody BuyGoodsRequest request) {
        request.setUserId(StpUtil.getLoginIdAsInt());
        return userGoodsService.buyGoods(request, StpUtil.getLoginIdAsInt());
    }
}