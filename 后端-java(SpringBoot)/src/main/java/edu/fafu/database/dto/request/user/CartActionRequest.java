package edu.fafu.database.dto.request.user;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CartActionRequest {
    @NotNull(message = "商品ID不能为空")
    private Integer goodId;

    @NotNull(message = "数量不能为空")
    @Min(value = 0, message = "数量不能小于0")
    private Integer num;
}