package edu.fafu.database.dto.request.page;

import edu.fafu.database.dto.request.common.PageRequest;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PageManagerUserRequest extends PageRequest {
    private Integer id;
    private String userName;
    private String account;
    private String phone;
    private String email;
    private String describe;
    private String imagePath;
    private Integer level;
    private LocalDateTime createTimeStart;
    private LocalDateTime createTimeEnd;
    private Boolean delete;
}