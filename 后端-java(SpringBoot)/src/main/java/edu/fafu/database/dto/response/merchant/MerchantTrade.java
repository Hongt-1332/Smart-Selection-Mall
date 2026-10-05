package edu.fafu.database.dto.response.merchant;

import edu.fafu.database.entity.Trade;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class MerchantTrade extends Trade {

    private Integer merchantUserId;

    private String userName;

    private String goodsName;

    private BigDecimal goodsPrice;

    private BigDecimal totalPrice;

    private BigDecimal priceMin;

    private BigDecimal priceMax;

    private LocalDateTime finishTimeStart;

    private LocalDateTime finishTimeEnd;

    private String originAddress;

    private String targetAddress;

    private String currentAddress;
}