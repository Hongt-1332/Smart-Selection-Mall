package edu.fafu.controller.businesscontroller.managercontroller;
import edu.fafu.database.entity.Address;
import edu.fafu.database.entity.Result;
import edu.fafu.service.service.businessservice.managerservice.ManagerAddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import cn.dev33.satoken.annotation.SaCheckRole;
import org.springframework.web.bind.annotation.RestController;

@SaCheckRole("admin")
@RestController
@RequestMapping("/manager/address")
public class ManagerAddressController {

    @Autowired
    private ManagerAddressService managerAddressService;

    @PutMapping
    public Result<String> updateAddress(@RequestBody Address address) {
        return managerAddressService.updateAddress(address);
    }
}