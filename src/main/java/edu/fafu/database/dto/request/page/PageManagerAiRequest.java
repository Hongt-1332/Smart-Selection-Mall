package edu.fafu.database.dto.request.page;

import edu.fafu.database.dto.request.common.PageRequest;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PageManagerAiRequest extends PageRequest {
    private Integer id;
    private String userName;
    private String category;
    private String kind;
    private String name;
    private String priceStart;
    private String priceEnd;
    private String simpleDescription;
    private String features;
    private LocalDateTime createTimeStart;
    private LocalDateTime createTimeEnd;
    private Boolean delete;
}