package edu.fafu.database.dto.response.user;

import edu.fafu.database.entity.VipTrade;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserVipTrade extends VipTrade {

    private Integer quantity;

    private BigDecimal addedTime;

    private LocalDateTime vipExpireTime;
}