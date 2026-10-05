package edu.fafu.controller.businesscontroller.vipcontroller;
import jakarta.validation.Valid;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import edu.fafu.database.entity.Result;
import edu.fafu.database.entity.VipConfig;
import edu.fafu.database.dto.response.common.ImageResponse;
import edu.fafu.database.dto.request.vip.VipBuyRequest;
import edu.fafu.database.dto.response.vip.VipLevelResponse;
import edu.fafu.database.dto.response.user.UserGoods;
import edu.fafu.service.service.businessservice.vipservice.VipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/vip")
public class VipController {

    private static final String VIP_LEVEL_SESSION_KEY = "vipLevel";

    @Autowired
    private VipService vipService;

    @GetMapping("/goods")
    public Result<List<UserGoods>> vipGoods() {
        Integer userId = StpUtil.isLogin() ? StpUtil.getLoginIdAsInt() : null;
        return vipService.vipGoods(userId);
    }

    @GetMapping("/image")
    public Result<List<ImageResponse>> vipImage() {
        return vipService.vipImage();
    }

    @GetMapping("/homeImage")
    public Result<List<ImageResponse>> vipHomeImage() {
        return vipService.vipHomeImage();
    }

    @SaCheckLogin
    @GetMapping("/level")
    public Result<VipLevelResponse> vipLevel() {
        Object vipLevelObj = StpUtil.getSession().get(VIP_LEVEL_SESSION_KEY);
        int vipLevel = vipLevelObj instanceof Integer ? (Integer) vipLevelObj : 0;
        return vipService.vipLevel(StpUtil.getLoginIdAsInt(), vipLevel);
    }

    @GetMapping("/config")
    public Result<List<VipConfig>> vipConfig() {
        return vipService.vipConfig();
    }

    @SaCheckLogin
    @PostMapping("/buy")
    public Result<String> buyVip(@Valid @RequestBody VipBuyRequest request) {
        return vipService.buyVip(request, StpUtil.getLoginIdAsInt());
    }
}