package edu.fafu.controller.businesscontroller.managercontroller;
import edu.fafu.database.entity.Result;
import edu.fafu.database.entity.VipTrade;
import edu.fafu.service.service.businessservice.managerservice.ManagerVipTradeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import cn.dev33.satoken.annotation.SaCheckRole;
import org.springframework.web.bind.annotation.RestController;

@SaCheckRole("admin")
@RestController
@RequestMapping("/manager/viptrade")
public class ManagerVipTradeController {

    @Autowired
    private ManagerVipTradeService managerVipTradeService;

    @PutMapping
    public Result<String> updateVipTrade(@RequestBody VipTrade vipTrade) {
        return managerVipTradeService.updateVipTrade(vipTrade);
    }
}