package edu.fafu.database.dto.request.common;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class PageRequest {
    @Positive(message = "页码必须为正数")
    private Integer page;

    @Min(value = 1, message = "每页数量不能小于1")
    @Max(value = 100, message = "每页数量不能超过100")
    private Integer size;

    private Boolean desc = false;
}