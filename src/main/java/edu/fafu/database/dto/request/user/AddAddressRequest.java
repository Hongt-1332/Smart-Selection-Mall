package edu.fafu.database.dto.request.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AddAddressRequest {

    @NotBlank(message = "国家不能为空")
    @Size(max = 50, message = "国家名称不能超过50个字符")
    private String country;

    @NotBlank(message = "省份不能为空")
    @Size(max = 50, message = "省份名称不能超过50个字符")
    private String province;

    @NotBlank(message = "城市不能为空")
    @Size(max = 50, message = "城市名称不能超过50个字符")
    private String city;

    @NotBlank(message = "区县不能为空")
    @Size(max = 50, message = "区县名称不能超过50个字符")
    private String county;

    @NotBlank(message = "详细地址不能为空")
    @Size(max = 200, message = "详细地址不能超过200个字符")
    private String detail;
}