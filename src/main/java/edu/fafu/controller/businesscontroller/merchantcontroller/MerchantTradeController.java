package edu.fafu.controller.businesscontroller.merchantcontroller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import edu.fafu.database.entity.Result;
import edu.fafu.service.service.businessservice.merchantservice.MerchantTradeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@SaCheckLogin
@RestController
@RequestMapping("/merchant/trade")
public class MerchantTradeController {

    @Autowired
    private MerchantTradeService merchantTradeService;

    @GetMapping("/get")
    public Result<?> getTrade(@RequestParam("id") Integer id) {
        return merchantTradeService.getTrade(id, StpUtil.getLoginIdAsInt());
    }

    @PutMapping("/deliver")
    public Result<String> deliverTrade(@RequestParam("id") Integer id) {
        return merchantTradeService.deliverTrade(id, StpUtil.getLoginIdAsInt());
    }

    @PutMapping("/confirm")
    public Result<String> confirmTrade(@RequestParam("id") Integer id) {
        return merchantTradeService.confirmTrade(id, StpUtil.getLoginIdAsInt());
    }

    @PutMapping("/cancel")
    public Result<String> cancelTrade(@RequestParam("id") Integer id) {
        return merchantTradeService.cancelTrade(id, StpUtil.getLoginIdAsInt());
    }

    @PutMapping("/finishTrade")
    public Result<String> finishTrade(@RequestParam("id") Integer id) {
        return merchantTradeService.finishTrade(id, StpUtil.getLoginIdAsInt());
    }

    @DeleteMapping("/delete")
    public Result<String> deleteTrade(@RequestParam("id") Integer id) {
        return merchantTradeService.deleteTrade(id, StpUtil.getLoginIdAsInt());
    }
}