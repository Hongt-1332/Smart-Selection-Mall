package edu.fafu.database.dto.response.user;

import edu.fafu.database.entity.Goods;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserGoods extends Goods {

    private String userName;

    private String merchantAvatar;

    private Integer merchantId;

    private String address;

    private Boolean inCart;

    private Integer cartQuantity;

    private BigDecimal priceMin;

    private BigDecimal priceMax;
}