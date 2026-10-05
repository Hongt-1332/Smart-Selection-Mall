package edu.fafu.controller.businesscontroller.managercontroller;
import edu.fafu.database.entity.Result;
import edu.fafu.database.entity.Trade;
import edu.fafu.service.service.businessservice.managerservice.ManagerTradeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import cn.dev33.satoken.annotation.SaCheckRole;
import org.springframework.web.bind.annotation.RestController;

@SaCheckRole("admin")
@RestController
@RequestMapping("/manager/trade")
public class ManagerTradeController {

    @Autowired
    private ManagerTradeService managerTradeService;

    @PutMapping
    public Result<String> updateTrade(@RequestBody Trade trade) {
        return managerTradeService.updateTrade(trade);
    }
}