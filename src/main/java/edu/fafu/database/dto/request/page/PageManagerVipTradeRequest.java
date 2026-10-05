package edu.fafu.database.dto.request.page;

import edu.fafu.database.dto.request.common.PageRequest;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PageManagerVipTradeRequest extends PageRequest {
    private Integer id;
    private String userName;
    private Integer level;
    private BigDecimal moneyStart;
    private BigDecimal moneyEnd;
    private LocalDateTime createTimeStart;
    private LocalDateTime createTimeEnd;
    private Boolean delete;
}