package edu.fafu.database.dto.request.merchant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AddAiRequest {

    @NotBlank(message = "分类不能为空")
    @Size(max = 50, message = "分类不能超过50个字符")
    private String category;

    @NotBlank(message = "种类不能为空")
    @Size(max = 50, message = "种类不能超过50个字符")
    private String kind;

    @NotBlank(message = "名称不能为空")
    @Size(max = 100, message = "名称不能超过100个字符")
    private String name;

    @NotBlank(message = "价格不能为空")
    @Size(max = 50, message = "价格格式不正确")
    private String price;

    @NotBlank(message = "简要描述不能为空")
    @Size(max = 500, message = "简要描述不能超过500个字符")
    private String simpleDescription;

    @NotBlank(message = "功能特性不能为空")
    @Size(max = 2000, message = "功能特性不能超过2000个字符")
    private String features;
}