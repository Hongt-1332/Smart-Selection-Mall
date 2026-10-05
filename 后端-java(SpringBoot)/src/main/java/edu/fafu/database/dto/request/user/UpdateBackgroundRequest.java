package edu.fafu.database.dto.request.user;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class UpdateBackgroundRequest {

    @NotNull(message = "背景ID不能为空")
    @Positive(message = "背景ID不合法")
    private Integer id;

    private String imagePath;

    private Integer sequence;
}