package edu.fafu.database.dto.response.merchant;

import edu.fafu.database.entity.Background;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class MerchantBackground extends Background {

    private String userName;
    private String avatarPath;
    private String describe;
    private Integer level;
    private Integer vipLevel;
}