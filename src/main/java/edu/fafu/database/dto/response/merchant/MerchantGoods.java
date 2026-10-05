package edu.fafu.database.dto.response.merchant;

import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;
import edu.fafu.tool.crypto.PathCryptoDeserializer;
import edu.fafu.tool.crypto.PathCryptoSerializer;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

public class MerchantGoods {
    private Integer id;
    private Integer userId;
    private String goodsName;
    private String describe;
    private BigDecimal goodsPrice;
    private Integer goodsStock;
    @JsonSerialize(using = PathCryptoSerializer.class)
    @JsonDeserialize(using = PathCryptoDeserializer.class)
    private String imagePath;
    private Integer launch;
    private Integer preId;
    private LocalDateTime createTime;
    private Integer delete;

    public MerchantGoods() {
    }

    public MerchantGoods(Integer id, Integer userId, String goodsName, String describe, BigDecimal goodsPrice, Integer goodsStock, String imagePath, Integer launch, Integer preId, LocalDateTime createTime, Integer delete) {
        this.id = id;
        this.userId = userId;
        this.goodsName = goodsName;
        this.describe = describe;
        this.goodsPrice = goodsPrice;
        this.goodsStock = goodsStock;
        this.imagePath = imagePath;
        this.launch = launch;
        this.preId = preId;
        this.createTime = createTime;
        this.delete = delete;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getGoodsName() {
        return goodsName;
    }

    public void setGoodsName(String goodsName) {
        this.goodsName = goodsName;
    }

    public String getDescribe() {
        return describe;
    }

    public void setDescribe(String describe) {
        this.describe = describe;
    }

    public BigDecimal getGoodsPrice() {
        return goodsPrice;
    }

    public void setGoodsPrice(BigDecimal goodsPrice) {
        this.goodsPrice = goodsPrice;
    }

    public Integer getGoodsStock() {
        return goodsStock;
    }

    public void setGoodsStock(Integer goodsStock) {
        this.goodsStock = goodsStock;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public Integer getLaunch() {
        return launch;
    }

    public void setLaunch(Integer launch) {
        this.launch = launch;
    }

    public Integer getPreId() {
        return preId;
    }

    public void setPreId(Integer preId) {
        this.preId = preId;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public Integer getDelete() {
        return delete;
    }

    public void setDelete(Integer delete) {
        this.delete = delete;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MerchantGoods that = (MerchantGoods) o;
        return Objects.equals(id, that.id) && Objects.equals(userId, that.userId) && Objects.equals(goodsName, that.goodsName) && Objects.equals(describe, that.describe) && Objects.equals(goodsPrice, that.goodsPrice) && Objects.equals(goodsStock, that.goodsStock) && Objects.equals(imagePath, that.imagePath) && Objects.equals(launch, that.launch) && Objects.equals(preId, that.preId) && Objects.equals(createTime, that.createTime) && Objects.equals(delete, that.delete);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userId, goodsName, describe, goodsPrice, goodsStock, imagePath, launch, preId, createTime, delete);
    }

    @Override
    public String toString() {
        return "MerchantGoods{" +
                "id=" + id +
                ", userId=" + userId +
                ", goodsName='" + goodsName + '\'' +
                ", describe='" + describe + '\'' +
                ", goodsPrice=" + goodsPrice +
                ", goodsStock=" + goodsStock +
                ", imagePath='" + imagePath + '\'' +
                ", launch=" + launch +
                ", preId=" + preId +
                ", createTime=" + createTime +
                ", delete=" + delete +
                '}';
    }
}