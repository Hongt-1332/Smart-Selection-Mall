package edu.fafu.database.dto.request.page;

import edu.fafu.database.dto.request.common.PageRequest;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PageManagerTradeRequest extends PageRequest {
    private Integer id;
    private String userName;
    private String merchantName;
    private String goodsName;
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
    private Boolean delete;
}