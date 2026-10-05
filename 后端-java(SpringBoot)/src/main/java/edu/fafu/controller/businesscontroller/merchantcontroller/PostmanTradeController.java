package edu.fafu.controller.businesscontroller.merchantcontroller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import edu.fafu.database.entity.Result;
import edu.fafu.service.service.businessservice.postmanservice.PostmanTradeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@SaCheckLogin
@RestController("merchantPostmanTradeController")
@RequestMapping("/postman/trade")
public class PostmanTradeController {

    @Autowired
    private PostmanTradeService postmanTradeService;

    @PutMapping("/deliver")
    public Result<String> deliverTrade(@RequestParam("id") Integer id) {
        return postmanTradeService.deliverTrade(id, StpUtil.getLoginIdAsInt());
    }

    @PutMapping("/confirm")
    public Result<String> confirmTrade(@RequestParam("id") Integer id) {
        return postmanTradeService.confirmTrade(id, StpUtil.getLoginIdAsInt());
    }

    @DeleteMapping("/delete")
    public Result<String> deleteTrade(@RequestParam("id") Integer id) {
        return postmanTradeService.deleteTrade(id, StpUtil.getLoginIdAsInt());
    }
}