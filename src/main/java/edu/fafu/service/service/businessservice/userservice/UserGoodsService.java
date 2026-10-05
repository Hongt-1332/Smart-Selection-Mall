package edu.fafu.service.service.businessservice.userservice;

import edu.fafu.database.entity.Result;
import edu.fafu.database.dto.request.user.BuyGoodsRequest;

public interface UserGoodsService {

    Result<String> buyGoods(BuyGoodsRequest request, Integer userId);
}