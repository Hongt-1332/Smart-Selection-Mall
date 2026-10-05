package edu.fafu.database.dto.request.merchant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AddMerchantGoodsRequest {

    @NotNull(message = "地址ID不能为空")
    private Integer addressId;

    @NotBlank(message = "商品名称不能为空")
    @Size(max = 100, message = "商品名称不能超过100个字符")
    private String goodsName;

    private String describe;

    @NotNull(message = "商品价格不能为空")
    @PositiveOrZero(message = "商品价格不能小于0")
    private BigDecimal goodsPrice;

    @NotNull(message = "商品库存不能为空")
    @PositiveOrZero(message = "商品库存不能小于0")
    private Integer goodsStock;
}