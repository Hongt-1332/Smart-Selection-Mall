package edu.fafu.controller.businesscontroller.postmancontroller;
import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import edu.fafu.database.entity.Result;
import edu.fafu.database.entity.Trade;
import edu.fafu.service.service.businessservice.postmanservice.PostmanTradeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SaCheckLogin
@RestController
@RequestMapping("/postman")
public class PostmanTradeController {

    @Autowired
    private PostmanTradeService postmanTradeService;

    @PostMapping("/updateCurrentAddress")
    public Result<String> updateCurrentAddress(@RequestBody Trade trade) {
        return postmanTradeService.updateCurrentAddress(trade, StpUtil.getLoginIdAsInt());
    }
}