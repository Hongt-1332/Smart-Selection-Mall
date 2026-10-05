package edu.fafu.database.dto.request.page;

import edu.fafu.database.dto.request.common.PageRequest;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PageManagerVipConfigRequest extends PageRequest {
    private Integer level;
    private Integer maxAddressQuantity;
    private Integer monthlyUpdateGoods;
    private Integer monthlyUpdateAvatar;
    private Integer monthlyUpdateBackground;
    private Integer maxCartQuantity;
    private Integer maxGoodsQuantity;
    private Integer maxAiQuantity;
    private BigDecimal price;
}