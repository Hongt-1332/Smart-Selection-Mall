package edu.fafu.database.dto.response.manager;

import edu.fafu.database.entity.User;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class ManagerUser extends User {

    private LocalDateTime createTimeStart;
    private LocalDateTime createTimeEnd;
    private Boolean delete;
    private String deleteStartTime;
    private String deleteEndTime;
    private String imagePath;
    private Integer userCount;
    private Integer vipUserCount;
    private Integer merchantCount;
}