package edu.fafu.controller.businesscontroller.managercontroller;
import edu.fafu.database.entity.Ai;
import edu.fafu.database.entity.Result;
import edu.fafu.service.service.businessservice.managerservice.ManagerAiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import cn.dev33.satoken.annotation.SaCheckRole;
import org.springframework.web.bind.annotation.RestController;

@SaCheckRole("admin")
@RestController
@RequestMapping("/manager/ai")
public class ManagerAiController {

    @Autowired
    private ManagerAiService managerAiService;

    @PutMapping("/updateAi")
    public Result<String> updateAi(@RequestBody Ai ai) {
        return managerAiService.updateAi(ai);
    }
}