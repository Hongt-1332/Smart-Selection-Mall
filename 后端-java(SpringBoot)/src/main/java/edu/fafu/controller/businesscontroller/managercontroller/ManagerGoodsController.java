package edu.fafu.controller.businesscontroller.managercontroller;
import jakarta.validation.Valid;

import edu.fafu.database.dto.request.manager.UpdateManagerGoodsRequest;
import edu.fafu.database.entity.Result;
import edu.fafu.service.service.businessservice.managerservice.ManagerGoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import cn.dev33.satoken.annotation.SaCheckRole;
import org.springframework.web.bind.annotation.RestController;

@SaCheckRole("admin")
@RestController
@RequestMapping("/manager/goods")
public class ManagerGoodsController {

    @Autowired
    private ManagerGoodsService managerGoodsService;

    @PutMapping
    public Result<String> updateGoods(@Valid @RequestBody UpdateManagerGoodsRequest request) {
        return managerGoodsService.updateGoods(request);
    }
}