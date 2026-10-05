package edu.fafu.database.dto.request.page;

import edu.fafu.database.dto.request.common.PageRequest;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PageManagerAddressRequest extends PageRequest {
    private Integer id;
    private String userName;
    private LocalDateTime createTimeStart;
    private LocalDateTime createTimeEnd;
    private Boolean delete;
}