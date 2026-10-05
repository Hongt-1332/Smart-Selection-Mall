package edu.fafu.database.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;
import edu.fafu.tool.crypto.PathCryptoDeserializer;
import edu.fafu.tool.crypto.PathCryptoSerializer;
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
@TableName("user")
public class User {

    public static final String ROLE_USER = "user";

    @TableId
    private Integer id;

    @TableField("userName")
    private String userName;

    private String account;

    private String password;

    private String salt;

    private String role;

    @TableField("wechatOpenid")
    private String wechatOpenid;  // 微信小程序 openid

    @TableField("defaultAddressId")
    private Integer defaultAddressId;

    private BigDecimal balance;

    private String email;

    private String phone;

    @TableField("`describe`")
    private String describe;

    @JsonSerialize(using = PathCryptoSerializer.class)
    @JsonDeserialize(using = PathCryptoDeserializer.class)
    @TableField("avatarPath")
    private String avatarPath;

    @JsonSerialize(using = PathCryptoSerializer.class)
    @JsonDeserialize(using = PathCryptoDeserializer.class)
    @TableField("imagePath")
    private String imagePath;

    private Integer level;

    @TableField("vipCreateTime")
    private LocalDateTime vipCreateTime;

    private Integer vipDuration;

    @TableField("createTime")
    private LocalDateTime createTime;

    @TableField("`delete`")
    @TableLogic
    private Boolean delete;

    @TableField("uploadTime")
    private LocalDateTime uploadTime;

    @TableField("updateAvatar")
    private Integer updateAvatar;

    @TableField("uploadBackground")
    private Integer uploadBackground;

    @TableField("uploadGoods")
    private Integer uploadGoods;

    public LocalDateTime getVipEndTime() {
        if (vipCreateTime == null || vipDuration == null) return null;
        return vipCreateTime.plusDays(vipDuration);
    }
}