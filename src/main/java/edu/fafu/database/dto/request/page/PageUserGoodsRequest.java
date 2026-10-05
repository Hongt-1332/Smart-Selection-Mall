package edu.fafu.database.dto.request.page;

import edu.fafu.database.dto.request.common.PageRequest;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PageUserGoodsRequest extends PageRequest {
    private String userName;
    private String goodsName;

    @Positive(message = "最低价格不合法")
    private BigDecimal priceMin;

    @Positive(message = "最高价格不合法")
    private BigDecimal priceMax;
}