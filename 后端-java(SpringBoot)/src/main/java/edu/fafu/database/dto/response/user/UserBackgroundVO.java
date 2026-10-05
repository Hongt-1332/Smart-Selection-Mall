package edu.fafu.database.dto.response.user;

import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;
import edu.fafu.tool.crypto.PathCryptoDeserializer;
import edu.fafu.tool.crypto.PathCryptoSerializer;
import lombok.Data;

import java.util.List;

@Data
public class UserBackgroundVO {

    private String userName;
    private String describe;
    private Integer level;
    private String levelName;
    private List<BackgroundImage> backgrounds;
    private List<UserBackgroundGoods> goods;
    private Integer defaultBackground;

    @Data
    public static class BackgroundImage {
        private Integer id;
        private String userName;
        @JsonSerialize(using = PathCryptoSerializer.class)
        @JsonDeserialize(using = PathCryptoDeserializer.class)
        private String imagePath;
        private String path;
    }

    @Data
    public static class UserBackgroundGoods {
        private Integer id;
        private String goodsName;
        private String imagePath;
        private String path;
    }
}