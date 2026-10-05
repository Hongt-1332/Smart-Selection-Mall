package edu.fafu.database.dto.request.page;

import edu.fafu.database.dto.request.common.PageRequest;
import lombok.Data;

@Data
public class PageMerchantUserRequest extends PageRequest {
    private String userName;
}