package edu.fafu.database.dto.request.page;

import edu.fafu.database.dto.request.common.PageRequest;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PageManagerGoodsRequest extends PageRequest {
    private Integer id;
    private String merchantName;
    private String goodsName;
    private String address;
    private BigDecimal priceStart;
    private BigDecimal priceEnd;
    private String describe;
    private Boolean launch;
    private LocalDateTime createTimeStart;
    private LocalDateTime createTimeEnd;
    private Boolean delete;
}