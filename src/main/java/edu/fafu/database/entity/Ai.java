package edu.fafu.database.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("ai")
public class Ai {

    @TableId
    private Integer id;
    @TableField("userId")
    private Integer userId;
    @TableField("goodsId")
    private Integer goodsId;
    private String category;
    private String kind;
    private String name;
    private String price;
    @TableField("simpleDescription")
    private String simpleDescription;
    private String features;
    @TableField("createTime")
    private LocalDateTime createTime;

    @TableField("`delete`")
    @TableLogic
    private Boolean delete;

    public String toTextSegment() {
        return """
                商品编号 %s
                产品类型 %s
                产品种类 %s
                产品名称 %s
                产品价格 %s
                产品简介 %s
                产品特点 %s""".formatted(goodsId, category, kind, name, price, simpleDescription, features).stripIndent();
    }


}