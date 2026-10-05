package edu.fafu.database.dto.request.manager;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UpdateManagerBackgroundRequest {

    @NotNull(message = "背景ID不能为空")
    @Positive(message = "背景ID不合法")
    private Integer id;

    private String imagePath;

    private LocalDateTime createTime;
}