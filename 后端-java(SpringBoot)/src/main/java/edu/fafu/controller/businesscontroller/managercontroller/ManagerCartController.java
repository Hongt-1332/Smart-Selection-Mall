package edu.fafu.controller.businesscontroller.managercontroller;

import edu.fafu.database.entity.Result;
import edu.fafu.service.service.businessservice.managerservice.ManagerCartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import cn.dev33.satoken.annotation.SaCheckRole;
import org.springframework.web.bind.annotation.RestController;

@SaCheckRole("admin")
@RestController
@RequestMapping("/manager/cart")
public class ManagerCartController {

    @Autowired
    private ManagerCartService managerCartService;

    @DeleteMapping("/delete")
    public Result<String> deleteCart(@RequestParam Integer id) {
        return managerCartService.deleteCart(id);
    }
}