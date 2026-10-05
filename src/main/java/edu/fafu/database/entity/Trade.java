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
@TableName("trade")
public class Trade {

    @TableId
    private Integer id;

    @TableField("orderId")
    private String orderId;

    @TableField("userId")
    private Integer userId;

    @TableField("goodId")
    private Integer goodId;

    private Integer quantity;

    @TableField("originAddress")
    private String originAddress;

    @TableField("currentAddress")
    private String currentAddress;

    @TableField("targetAddress")
    private String targetAddress;

    @TableField("createTime")
    private LocalDateTime createTime;

    @TableField("payTime")
    private LocalDateTime payTime;

    @TableField("cancelTime")
    private LocalDateTime cancelTime;

    @TableField("finishTime")
    private LocalDateTime finishTime;

    @TableField("`delete`")
    @TableLogic
    private Boolean delete;
}