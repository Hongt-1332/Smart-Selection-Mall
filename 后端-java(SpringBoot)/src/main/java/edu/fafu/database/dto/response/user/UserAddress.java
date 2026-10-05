package edu.fafu.database.dto.response.user;

import edu.fafu.database.entity.Address;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserAddress extends Address {

    private String userName;
    private Boolean isDefault;
}