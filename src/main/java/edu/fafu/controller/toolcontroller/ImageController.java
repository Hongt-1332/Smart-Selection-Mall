package edu.fafu.controller.toolcontroller;
import cn.dev33.satoken.annotation.SaCheckLogin;
import edu.fafu.database.entity.Result;
import edu.fafu.database.dto.response.common.ImageResponse;
import edu.fafu.service.service.toolservice.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@SaCheckLogin
@RestController
@RequestMapping("/image")
public class ImageController {

    @Autowired
    private ImageService imageService;

    @PostMapping("/getImages")
    public Result<List<ImageResponse>> getImages(@RequestBody List<String> paths) {
        return imageService.getImages(paths);
    }
}