package edu.fafu.database.dto.response.vip;

import edu.fafu.database.entity.User;
import edu.fafu.database.entity.VipConfig;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
public class VipLevelResponse {
    private Integer level;
    private String name;
    private BigDecimal price;
    private String description;

    public VipLevelResponse() {
    }

    public VipLevelResponse(User user, Map<Integer, VipConfig> configMap) {
        this.level = user.getLevel();
        if (user.getLevel() != null && configMap != null) {
            VipConfig config = configMap.get(user.getLevel());
            if (config != null) {
                this.name = config.getName();
                this.price = config.getPrice();
            }
        }
    }
}