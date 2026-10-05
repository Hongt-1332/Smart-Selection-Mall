package edu.fafu.database.dto.response.manager;

import edu.fafu.database.entity.Address;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class ManagerAddress extends Address {

    private String userName;
    private LocalDateTime createTimeStart;
    private LocalDateTime createTimeEnd;
}