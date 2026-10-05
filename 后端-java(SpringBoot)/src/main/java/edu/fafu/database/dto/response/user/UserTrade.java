package edu.fafu.database.dto.response.user;

import edu.fafu.database.entity.Trade;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserTrade extends Trade {

    private String merchantName;

    private String goodsName;

    private BigDecimal goodsPrice;

    private BigDecimal totalPrice;

    private LocalDateTime createTimeStart;

    private LocalDateTime createTimeEnd;
}