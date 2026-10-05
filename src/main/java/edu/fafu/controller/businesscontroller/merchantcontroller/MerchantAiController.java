package edu.fafu.controller.businesscontroller.merchantcontroller;
import jakarta.validation.Valid;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import edu.fafu.database.entity.Result;
import edu.fafu.database.dto.request.merchant.AddAiRequest;
import edu.fafu.database.dto.request.merchant.UpdateAiRequest;
import edu.fafu.service.service.businessservice.merchantservice.MerchantAiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@SaCheckLogin
@RestController
@RequestMapping("/merchant/ai")
public class MerchantAiController {

    @Autowired
    private MerchantAiService merchantAiService;

    @PostMapping("/add")
    public Result<String> addAi(@Valid @RequestBody AddAiRequest request) {
        return merchantAiService.addAi(request, StpUtil.getLoginIdAsInt());
    }

    @PutMapping("/update")
    public Result<String> updateAi(@Valid @RequestBody UpdateAiRequest request) {
        return merchantAiService.updateAi(request, StpUtil.getLoginIdAsInt());
    }

    @GetMapping("/goodsList")
    public Result<?> goodsList() {
        return merchantAiService.goodsList(StpUtil.getLoginIdAsInt());
    }

    @DeleteMapping("/delete")
    public Result<String> deleteAi(@RequestParam("id") Integer id) {
        return merchantAiService.deleteAi(id, StpUtil.getLoginIdAsInt());
    }
}