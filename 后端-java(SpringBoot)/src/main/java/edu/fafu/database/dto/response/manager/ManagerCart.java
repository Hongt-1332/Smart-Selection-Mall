package edu.fafu.database.dto.response.manager;

import edu.fafu.database.entity.Goods;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class ManagerCart extends Goods {

    private String userName;
    private String goodsName;
    private Integer goodsStock;
    private Integer quantity;
    private Boolean launch;
    private LocalDateTime createTimeStart;
    private LocalDateTime createTimeEnd;
}