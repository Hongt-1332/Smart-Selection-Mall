package edu.fafu.database.dto.request.user;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateAddressRequest {

    @NotNull(message = "地址ID不能为空")
    @Positive(message = "地址ID不合法")
    private Integer id;

    @Size(max = 50, message = "国家名称不能超过50个字符")
    private String country;

    @Size(max = 50, message = "省份名称不能超过50个字符")
    private String province;

    @Size(max = 50, message = "城市名称不能超过50个字符")
    private String city;

    @Size(max = 50, message = "区县名称不能超过50个字符")
    private String county;

    @Size(max = 200, message = "详细地址不能超过200个字符")
    private String detail;
}