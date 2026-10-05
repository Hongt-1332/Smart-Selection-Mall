package edu.fafu.database.dto.response.user;

import edu.fafu.database.entity.Goods;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserCart extends Goods {

    private Integer cartId;
    private Integer quantity;

    private String userName;

    private String merchantAvatar;

    private Integer merchantId;

    private String address;
}