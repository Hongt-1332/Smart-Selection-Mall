package edu.fafu.database.dto.response.user;

import edu.fafu.database.entity.Background;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserBackground extends Background {

    private String describe;
}