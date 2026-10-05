package edu.fafu.database.dto.request.user;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateDescribeRequest {
    @Size(max = 200, message = "个人简介长度不能超过200个字符")
    private String describe;
}