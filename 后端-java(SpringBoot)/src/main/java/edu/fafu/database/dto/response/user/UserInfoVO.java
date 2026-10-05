package edu.fafu.database.dto.response.user;

import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;
import edu.fafu.tool.crypto.PathCryptoDeserializer;
import edu.fafu.tool.crypto.PathCryptoSerializer;
import lombok.Data;

import java.util.List;

@Data
public class UserInfoVO {

    private Integer id;
    private String userName;
    private String account;
    private String phone;
    private String email;
    private String avatarPath;
    private String describe;
    private String role;
    private Integer level;
    private String levelName;
    private Long remainingTime;
    @JsonSerialize(using = PathCryptoSerializer.class)
    @JsonDeserialize(using = PathCryptoDeserializer.class)
    private String imagePath;
    private List<AddressItem> addresses;

    @Data
    public static class AddressItem {
        private Integer id;
        private String address;
        private Boolean isDefault;
    }
}