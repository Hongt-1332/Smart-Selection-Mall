package edu.fafu.database.dto.response.user;

import lombok.Data;

@Data
public class WechatLoginResponse {
    private String token;
    private Integer userId;
    private Boolean isNewUser;  // 是否是新注册用户
}