package edu.fafu.controller.businesscontroller.usercontroller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import edu.fafu.database.entity.Result;
import edu.fafu.service.service.businessservice.userservice.UserTradeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@SaCheckLogin
@RestController
@RequestMapping("/user/trade")
public class UserTradeController {

    @Autowired
    private UserTradeService userTradeService;

    @PutMapping("/cancel")
    public Result<String> cancelTrade(@RequestParam("id") Integer id) {
        return userTradeService.cancelTrade(id, StpUtil.getLoginIdAsInt());
    }
}