package edu.fafu.database.dto.request.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class WechatLoginRequest {
    @NotBlank(message = "微信登录凭证不能为空")
    private String code;  // wx.login() 返回的临时凭证
}