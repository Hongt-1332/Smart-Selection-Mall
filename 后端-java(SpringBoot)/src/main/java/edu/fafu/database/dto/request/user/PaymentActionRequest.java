package edu.fafu.database.dto.request.user;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PaymentActionRequest {

    @NotNull(message = "ID不能为空")
    private Integer id;
}