package edu.fafu.controller.toolcontroller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import edu.fafu.config.SystemConfig;
import edu.fafu.database.entity.Result;
import edu.fafu.tool.captcha.CaptchaUtil;
import edu.fafu.service.service.toolservice.ToolService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import static edu.fafu.tool.captcha.CaptchaUtil.generate;

@RestController
@RequestMapping("/tool")
public class ToolController {

    @Autowired
    private ToolService toolService;
    @Autowired
    private SystemConfig systemConfig;

    @GetMapping("/captcha")
    public Result<CaptchaUtil.VO> captcha() {
        return Result.success(generate(systemConfig));
    }

    @SaCheckLogin
    @PostMapping("/image")
    public Result<String> uploadImage(@RequestParam("file") MultipartFile file) {
        return toolService.uploadImage(file, StpUtil.getLoginIdAsInt());
    }
}