package edu.fafu.database.dto.request.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddBackgroundRequest {

    @NotBlank(message = "图片路径不能为空")
    private String imagePath;

    private Integer sequence;
}