package edu.fafu.database.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("cart")
public class Cart {

    @TableId
    @JsonAlias("cartId")
    private Integer id;

    @TableField("userId")
    private Integer userId;

    @TableField("goodId")
    private Integer goodId;

    private Integer quantity;

    @TableField("createTime")
    private LocalDateTime createTime;
}