package edu.fafu.database.dto.response.manager;

import edu.fafu.database.entity.Ai;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class ManagerAi extends Ai {

    private String userName;
    private String userNameLink;
    private String priceStart;
    private String priceEnd;
    private LocalDateTime createTimeStart;
    private LocalDateTime createTimeEnd;
}