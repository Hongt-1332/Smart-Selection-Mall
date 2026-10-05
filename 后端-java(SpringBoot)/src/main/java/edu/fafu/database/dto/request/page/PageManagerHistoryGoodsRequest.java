package edu.fafu.database.dto.request.page;

import edu.fafu.database.dto.request.common.PageRequest;
import lombok.Data;

@Data
public class PageManagerHistoryGoodsRequest extends PageRequest {
    private String goodsId;
}