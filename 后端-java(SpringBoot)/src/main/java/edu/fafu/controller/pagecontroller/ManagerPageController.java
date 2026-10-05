package edu.fafu.controller.pagecontroller;
import jakarta.validation.Valid;

import edu.fafu.database.entity.Result;
import edu.fafu.database.dto.request.page.*;
import edu.fafu.database.dto.response.manager.*;
import edu.fafu.service.service.pageservice.ManagerPageService;
import edu.fafu.tool.page.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import cn.dev33.satoken.annotation.SaCheckRole;
import org.springframework.web.bind.annotation.RestController;

@SaCheckRole("admin")
@RestController
@RequestMapping("/page/manager")
public class ManagerPageController {

    @Autowired
    private ManagerPageService managerPageService;

    @PostMapping("/user")
    public Result<PageResult<ManagerUser>> pageManagerUser(@Valid @RequestBody PageManagerUserRequest request) {
        return managerPageService.pageManagerUser(request);
    }

    @PostMapping("/address")
    public Result<PageResult<ManagerAddress>> pageManagerAddress(@Valid @RequestBody PageManagerAddressRequest request) {
        return managerPageService.pageManagerAddress(request);
    }

    @PostMapping("/cart")
    public Result<PageResult<ManagerCart>> pageManagerCart(@Valid @RequestBody PageManagerCartRequest request) {
        return managerPageService.pageManagerCart(request);
    }

    @PostMapping("/goods")
    public Result<PageResult<ManagerGoods>> pageManagerGoods(@Valid @RequestBody PageManagerGoodsRequest request) {
        return managerPageService.pageManagerGoods(request);
    }

    @PostMapping("/trade")
    public Result<PageResult<ManagerTrade>> pageManagerTrade(@Valid @RequestBody PageManagerTradeRequest request) {
        return managerPageService.pageManagerTrade(request);
    }

    @PostMapping("/vipConfig")
    public Result<PageResult<ManagerVipConfig>> pageManagerVipConfig(@Valid @RequestBody PageManagerVipConfigRequest request) {
        return managerPageService.pageManagerVipConfig(request);
    }

    @PostMapping("/vipTrade")
    public Result<PageResult<ManagerVipTrade>> pageManagerVipTrade(@Valid @RequestBody PageManagerVipTradeRequest request) {
        return managerPageService.pageManagerVipTrade(request);
    }

    @PostMapping("/ai")
    public Result<PageResult<ManagerAi>> pageManagerAi(@Valid @RequestBody PageManagerAiRequest request) {
        return managerPageService.pageManagerAi(request);
    }

    @PostMapping("/historyGoods")
    public Result<PageResult<ManagerGoods>> pageManagerHistoryGoods(@Valid @RequestBody PageManagerHistoryGoodsRequest request) {
        return managerPageService.pageManagerHistoryGoods(request);
    }
}