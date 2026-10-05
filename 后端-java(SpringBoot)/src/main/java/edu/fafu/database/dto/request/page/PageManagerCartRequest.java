package edu.fafu.database.dto.request.page;

import edu.fafu.database.dto.request.common.PageRequest;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PageManagerCartRequest extends PageRequest {
    private Integer id;
    private String userName;
    private String goodsName;
    private Integer goodsStock;
    private Integer quantity;
    private Boolean launch;
    private LocalDateTime createTimeStart;
    private LocalDateTime createTimeEnd;
}