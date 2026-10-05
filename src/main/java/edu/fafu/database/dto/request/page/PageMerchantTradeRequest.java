package edu.fafu.database.dto.request.page;

import edu.fafu.database.dto.request.common.PageRequest;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PageMerchantTradeRequest extends PageRequest {
    private String userName;
    private String goodsName;
    private BigDecimal priceMin;
    private BigDecimal priceMax;
    private String originAddress;
    private String targetAddress;
    private String currentAddress;
    private LocalDateTime createTimeStart;
    private LocalDateTime createTimeEnd;
    private LocalDateTime payTimeStart;
    private LocalDateTime payTimeEnd;
    private LocalDateTime cancelTimeStart;
    private LocalDateTime cancelTimeEnd;
    private LocalDateTime finishTimeStart;
    private LocalDateTime finishTimeEnd;
}