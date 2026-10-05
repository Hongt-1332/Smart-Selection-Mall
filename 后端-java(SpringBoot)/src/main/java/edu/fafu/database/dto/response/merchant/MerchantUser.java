package edu.fafu.database.dto.response.merchant;

import edu.fafu.database.entity.User;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class MerchantUser extends User {

    private Integer goodsCount;
}