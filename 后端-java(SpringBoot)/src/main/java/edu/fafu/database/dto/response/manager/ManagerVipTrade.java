package edu.fafu.database.dto.response.manager;

import edu.fafu.database.entity.VipTrade;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class ManagerVipTrade extends VipTrade {

    private String userName;
    private Integer level;
    private BigDecimal moneyStart;
    private BigDecimal moneyEnd;
    private LocalDateTime createTimeStart;
    private LocalDateTime createTimeEnd;
    private Boolean delete;
}