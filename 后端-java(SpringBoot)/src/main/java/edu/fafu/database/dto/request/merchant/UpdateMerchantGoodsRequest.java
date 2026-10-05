package edu.fafu.database.dto.request.merchant;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateMerchantGoodsRequest {

    @NotNull(message = "商品ID不能为空")
    @Positive(message = "商品ID不合法")
    private Integer id;

    @Size(max = 100, message = "商品名称不能超过100个字符")
    private String goodsName;

    private String describe;

    @PositiveOrZero(message = "商品价格不能小于0")
    private BigDecimal goodsPrice;

    @PositiveOrZero(message = "商品库存不能小于0")
    private Integer goodsStock;

    private Boolean launch;
}