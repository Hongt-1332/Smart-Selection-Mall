package edu.fafu.database.dto.response.user;

import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;
import edu.fafu.database.entity.User;
import edu.fafu.tool.crypto.PathCryptoDeserializer;
import edu.fafu.tool.crypto.PathCryptoSerializer;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserUser extends User {

    private String vipLevelName;

    private Long vipRemainingTime;

    @JsonSerialize(using = PathCryptoSerializer.class)
    @JsonDeserialize(using = PathCryptoDeserializer.class)
    private String avatarPath;

    @JsonSerialize(using = PathCryptoSerializer.class)
    @JsonDeserialize(using = PathCryptoDeserializer.class)
    private String imagePath;

    private List<BackgroundImage> backgrounds;

    @Data
    public static class BackgroundImage {
        private String imagePath;
        private Integer sequence;
    }
}