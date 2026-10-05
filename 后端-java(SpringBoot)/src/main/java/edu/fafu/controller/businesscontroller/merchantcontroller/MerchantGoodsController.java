package edu.fafu.controller.businesscontroller.merchantcontroller;
import jakarta.validation.Valid;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import edu.fafu.database.entity.Result;
import edu.fafu.database.dto.request.merchant.AddMerchantGoodsRequest;
import edu.fafu.database.dto.request.merchant.UpdateMerchantGoodsRequest;
import edu.fafu.database.dto.response.merchant.MerchantGoods;
import edu.fafu.service.service.businessservice.merchantservice.MerchantGoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@SaCheckLogin
@RestController
@RequestMapping("/merchant/goods")
public class MerchantGoodsController {

    @Autowired
    private MerchantGoodsService merchantGoodsService;

    @PostMapping("/add")
    public Result<String> addGoods(@Valid AddMerchantGoodsRequest request,
                                   @RequestParam("file") MultipartFile file) {
        return merchantGoodsService.addGoods(request, file, StpUtil.getLoginIdAsInt());
    }

    @PutMapping("/update")
    public Result<String> updateGoods(@Valid @RequestBody UpdateMerchantGoodsRequest request) {
        return merchantGoodsService.updateGoods(request, StpUtil.getLoginIdAsInt());
    }

    @PutMapping("/updateImage")
    public Result<String> updateImage(@RequestParam("id") Integer id,
                                      @RequestParam("file") MultipartFile file) {
        return merchantGoodsService.updateImage(id, file, StpUtil.getLoginIdAsInt());
    }

    @PutMapping("/updateDescribe")
    public Result<String> updateDescribe(@RequestParam("id") Integer id,
                                         @RequestParam("describe") String describe) {
        return merchantGoodsService.updateDescribe(id, describe, StpUtil.getLoginIdAsInt());
    }

    @PutMapping("/updateLaunch")
    public Result<String> updateLaunch(@RequestParam("id") Integer id,
                                       @RequestParam("launch") Boolean launch) {
        return merchantGoodsService.updateLaunch(id, launch, StpUtil.getLoginIdAsInt());
    }

    @DeleteMapping("/delete")
    public Result<String> deleteGoods(@RequestParam("id") Integer id) {
        return merchantGoodsService.deleteGoods(id, StpUtil.getLoginIdAsInt());
    }

    @GetMapping("/get")
    public Result<MerchantGoods> getGoods(@RequestParam("id") Integer id) {
        return merchantGoodsService.getGoods(id, StpUtil.getLoginIdAsInt());
    }

    @GetMapping("/count")
    public Result<Integer> getGoodsCount() {
        return merchantGoodsService.getGoodsCount(StpUtil.getLoginIdAsInt());
    }
}