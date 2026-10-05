package edu.fafu.database.dto.response.manager;

import edu.fafu.database.entity.Trade;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class ManagerTrade extends Trade {

    private String userName;
    private String merchantName;
    private String goodsName;
    private BigDecimal goodsPrice;
    private Integer quantity;
    private BigDecimal totalPrice;
    private LocalDateTime payTime;
    private LocalDateTime cancelTime;
    private LocalDateTime finishTime;
    private LocalDateTime createTimeStart;
    private LocalDateTime createTimeEnd;
    private LocalDateTime payTimeStart;
    private LocalDateTime payTimeEnd;
    private LocalDateTime cancelTimeStart;
    private LocalDateTime cancelTimeEnd;
    private LocalDateTime finishTimeStart;
    private LocalDateTime finishTimeEnd;
    private Boolean delete;
}