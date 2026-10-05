package edu.fafu.database.dto.request.merchant;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateAiRequest {

    @NotNull(message = "AI ID不能为空")
    @Positive(message = "AI ID不合法")
    private Integer id;

    @Size(max = 50, message = "分类不能超过50个字符")
    private String category;

    @Size(max = 50, message = "种类不能超过50个字符")
    private String kind;

    @Size(max = 100, message = "名称不能超过100个字符")
    private String name;

    @Size(max = 50, message = "价格格式不正确")
    private String price;

    @Size(max = 500, message = "简要描述不能超过500个字符")
    private String simpleDescription;

    @Size(max = 2000, message = "功能特性不能超过2000个字符")
    private String features;
}