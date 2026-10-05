package edu.fafu.controller.pagecontroller;
import jakarta.validation.Valid;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import edu.fafu.database.entity.Result;
import edu.fafu.database.dto.request.page.*;
import edu.fafu.database.dto.response.merchant.*;
import edu.fafu.service.service.pageservice.MerchantPageService;
import edu.fafu.tool.page.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/page/merchant")
@SaCheckLogin
public class MerchantPageController {

    @Autowired
    private MerchantPageService merchantPageService;

    @PostMapping("/goods")
    public Result<PageResult<MerchantGoods>> pageMerchantGoods(@Valid @RequestBody PageMerchantGoodsRequest request) {
        return merchantPageService.pageMerchantGoods(request, StpUtil.getLoginIdAsInt());
    }

    @PostMapping("/trade")
    public Result<PageResult<MerchantTrade>> pageMerchantTrade(@Valid @RequestBody PageMerchantTradeRequest request) {
        return merchantPageService.pageMerchantTrade(request, StpUtil.getLoginIdAsInt());
    }

    @PostMapping("/user")
    public Result<PageResult<MerchantUser>> pageMerchantUser(@Valid @RequestBody PageMerchantUserRequest request) {
        return merchantPageService.pageMerchantUser(request);
    }

    @PostMapping("/background")
    public Result<PageResult<MerchantBackground>> pageMerchantBackground(@Valid @RequestBody PageMerchantBackgroundRequest request) {
        return merchantPageService.pageMerchantBackground(request, StpUtil.getLoginIdAsInt());
    }

    @GetMapping("/merchantBackground")
    public Result<MerchantBackgroundVO> merchantBackground(@RequestParam("id") Integer id) {
        return merchantPageService.merchantBackground(id, StpUtil.getLoginIdAsInt());
    }

    @PostMapping("/ai")
    public Result<PageResult<MerchantAi>> pageMerchantAi(@Valid @RequestBody PageMerchantAiRequest request) {
        return merchantPageService.pageMerchantAi(request, StpUtil.getLoginIdAsInt());
    }
}