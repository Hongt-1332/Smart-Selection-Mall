package edu.fafu.database.dto.response.ai;

import edu.fafu.database.dto.response.user.UserGoods;
import lombok.Data;

import java.util.List;

@Data
public class ChatResponse {
    private String message;
    private List<UserGoods> products;

    public ChatResponse() {
    }

    public ChatResponse(String message, List<UserGoods> products) {
        this.message = message;
        this.products = products;
    }

}