package edu.fafu.database.dto.response.manager;

import edu.fafu.database.entity.Goods;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class ManagerGoods extends Goods {

    private String userName;
    private String userAccount;
    private String merchantName;
    private String goodsName;
    private BigDecimal priceStart;
    private BigDecimal priceEnd;
    private String address;
    private Boolean launch;
    private Boolean delete;
    private LocalDateTime createTimeStart;
    private LocalDateTime createTimeEnd;
}