package edu.fafu.database.dto.request.user;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class BuyGoodsRequest {
    @NotNull(message = "商品ID不能为空")
    @Positive(message = "商品ID不合法")
    private Integer goodsId;

    @NotNull(message = "数量不能为空")
    @Min(value = 1, message = "数量不能小于1")
    private Integer quantity;

    @NotNull(message = "收货地址不能为空")
    @Positive(message = "收货地址不合法")
    private Integer shippingAddressId;

    private Integer userId;
}