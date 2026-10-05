package edu.fafu.database.dto.request.manager;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateManagerGoodsRequest {

    @NotNull(message = "商品ID不能为空")
    @Positive(message = "商品ID不合法")
    private Integer id;

    private Integer userId;

    private String goodsId;

    private Integer addressId;

    private String goodsName;

    private String describe;

    private BigDecimal goodsPrice;

    private Integer goodsStock;

    private String imagePath;

    private Boolean launch;

    private Boolean delete;
}