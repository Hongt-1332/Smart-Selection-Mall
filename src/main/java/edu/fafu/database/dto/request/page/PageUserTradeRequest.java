package edu.fafu.database.dto.request.page;

import edu.fafu.database.dto.request.common.PageRequest;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PageUserTradeRequest extends PageRequest {
    private String merchantName;
    private String goodsName;
    private LocalDateTime createTimeStart;
    private LocalDateTime createTimeEnd;
}