package edu.fafu.database.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
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
@TableName("goods")
public class Goods {

    @TableId
    private Integer id;

    @TableField("userId")
    private Integer userId;

    @TableField("goodsId")
    private String goodsId;

    @TableField("addressId")
    private Integer addressId;

    @TableField("preId")
    private Integer preId;

    @TableField("goodsName")
    private String goodsName;

    @TableField("`describe`")
    private String describe;

    @TableField("goodsPrice")
    private BigDecimal goodsPrice;

    @TableField("goodsStock")
    private Integer goodsStock;

    @JsonSerialize(using = PathCryptoSerializer.class)
    @JsonDeserialize(using = PathCryptoDeserializer.class)
    @TableField("imagePath")
    private String imagePath;

    private Boolean launch;

    @TableField("createTime")
    private LocalDateTime createTime;

    @TableField("`delete`")
    @TableLogic
    private Boolean delete;

    @Version
    private Integer version;
}