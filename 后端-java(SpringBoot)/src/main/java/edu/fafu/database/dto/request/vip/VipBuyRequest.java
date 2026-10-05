package edu.fafu.database.dto.request.vip;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class VipBuyRequest {

    @NotNull(message = "VIP等级不能为空")
    @Positive(message = "VIP等级不合法")
    private Integer level;

    @NotNull(message = "金额不能为空")
    @Min(value = 0, message = "金额不能为负数")
    private BigDecimal money;

    @NotNull(message = "数量不能为空")
    @Min(value = 1, message = "数量不能小于1")
    private Integer num;
}