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
@TableName("address")
public class Address {

    @TableId
    private Integer id;

    @TableField("userId")
    private Integer userId;

    @TableField("addressId")
    private Integer addressId;

    private String country;

    private String province;

    private String city;

    private String county;

    private String detail;

    @TableField("createTime")
    private LocalDateTime createTime;

    @TableField("`delete`")
    @TableLogic
    private Boolean delete;
}