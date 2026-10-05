package edu.fafu.database.dto.response.merchant;

import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;
import edu.fafu.database.dto.response.user.UserGoods;
import edu.fafu.tool.crypto.PathCryptoDeserializer;
import edu.fafu.tool.crypto.PathCryptoSerializer;
import lombok.Data;

import java.util.List;

@Data
public class MerchantBackgroundVO {

    private String userName;
    private String describe;
    private Integer level;
    private String levelName;
    private List<BackgroundImage> backgrounds;
    private List<UserGoods> goods;

    @Data
    public static class BackgroundImage {
        private Integer id;
        @JsonSerialize(using = PathCryptoSerializer.class)
        @JsonDeserialize(using = PathCryptoDeserializer.class)
        private String imagePath;
    }
}