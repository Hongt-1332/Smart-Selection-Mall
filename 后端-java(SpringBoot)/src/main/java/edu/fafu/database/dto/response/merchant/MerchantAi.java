package edu.fafu.database.dto.response.merchant;

import edu.fafu.database.entity.Ai;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class MerchantAi extends Ai {

    private String priceStart;
    private String priceEnd;
    private LocalDateTime createTimeStart;
    private LocalDateTime createTimeEnd;
}