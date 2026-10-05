package edu.fafu.controller.businesscontroller.managercontroller;
import edu.fafu.database.entity.Result;
import edu.fafu.database.entity.User;
import edu.fafu.service.service.businessservice.managerservice.ManagerUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import cn.dev33.satoken.annotation.SaCheckRole;
import org.springframework.web.bind.annotation.RestController;

@SaCheckRole("admin")
@RestController
@RequestMapping("/manager/user")
public class ManagerUserController {

    @Autowired
    private ManagerUserService managerUserService;

    @PutMapping
    public Result<String> updateUser(@RequestBody User user) {
        return managerUserService.updateUser(user);
    }
}