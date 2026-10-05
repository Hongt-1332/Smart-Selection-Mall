package edu.fafu.database.dto.response.manager;

import edu.fafu.database.entity.VipConfig;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ManagerVipConfig extends VipConfig {

    private String levelName;
    private Integer level;
    private Boolean delete;
}