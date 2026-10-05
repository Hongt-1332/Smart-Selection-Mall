package edu.fafu.database.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("vipconfig")
public class VipConfig {

    @TableId(type = IdType.INPUT)
    private Integer level;

    @TableField("maxAddressQuantity")
    private Integer maxAddressQuantity;

    @TableField("monthlyUpdateGoods")
    private Integer monthlyUpdateGoods;

    @TableField("monthlyUpdateAvatar")
    private Integer monthlyUpdateAvatar;

    @TableField("monthlyUpdateBackground")
    private Integer monthlyUpdateBackground;

    @TableField("maxCartQuantity")
    private Integer maxCartQuantity;

    @TableField("maxGoodsQuantity")
    private Integer maxGoodsQuantity;

    @TableField("maxAiQuantity")
    private Integer maxAiQuantity;

    private BigDecimal price;

    private Integer vipDuration;

    @TableField("levelName")
    private String levelName;
    @TableField("createTime")
    private LocalDateTime createTime;

    public String getName() {
        return levelName;
    }
}