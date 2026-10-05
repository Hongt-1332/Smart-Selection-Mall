package edu.fafu.controller.pagecontroller;
import jakarta.validation.Valid;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import edu.fafu.database.entity.Result;
import edu.fafu.database.dto.request.page.*;
import edu.fafu.database.dto.response.user.*;
import edu.fafu.service.service.pageservice.UserPageService;
import edu.fafu.tool.page.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/page/user")
public class UserPageController {

    @Autowired
    private UserPageService userPageService;

    @SaCheckLogin
    @PostMapping("/user")
    public Result<UserUser> pageUserUser() {
        return userPageService.pageUserUser(StpUtil.getLoginIdAsInt());
    }

    @SaCheckLogin
    @PostMapping("/address")
    public Result<PageResult<UserAddress>> pageUserAddress(@Valid @RequestBody PageUserAddressRequest request) {
        return userPageService.pageUserAddress(request, StpUtil.getLoginIdAsInt());
    }

    @SaCheckLogin
    @PostMapping("/cart")
    public Result<PageResult<UserCart>> pageUserCart(@Valid @RequestBody PageUserCartRequest request) {
        return userPageService.pageUserCart(request, StpUtil.getLoginIdAsInt());
    }

    @PostMapping("/goods")
    public Result<PageResult<UserGoods>> pageUserGoods(@Valid @RequestBody PageUserGoodsRequest request) {
        return userPageService.pageUserGoods(request, StpUtil.getLoginIdAsInt());
    }

    @SaCheckLogin
    @PostMapping("/trade")
    public Result<PageResult<UserTrade>> pageUserTrade(@Valid @RequestBody PageUserTradeRequest request) {
        return userPageService.pageUserTrade(request, StpUtil.getLoginIdAsInt());
    }

    @SaCheckLogin
    @PostMapping("/background")
    public Result<PageResult<UserBackground>> pageUserBackground(@Valid @RequestBody PageUserBackgroundRequest request) {
        return userPageService.pageUserBackground(request, StpUtil.getLoginIdAsInt());
    }

    @PostMapping("/vipConfig")
    public Result<PageResult<UserVipConfig>> pageUserVipConfig(@Valid @RequestBody PageUserVipConfigRequest request) {
        return userPageService.pageUserVipConfig(request);
    }

    @SaCheckLogin
    @PostMapping("/vipTrade")
    public Result<PageResult<UserVipTrade>> pageUserVipTrade(@Valid @RequestBody PageUserVipTradeRequest request) {
        return userPageService.pageUserVipTrade(request, StpUtil.getLoginIdAsInt());
    }

    @SaCheckLogin
    @PostMapping("/userBackground")
    public Result<UserBackgroundVO> userBackground() {
        return userPageService.userBackground(StpUtil.getLoginIdAsInt());
    }
}