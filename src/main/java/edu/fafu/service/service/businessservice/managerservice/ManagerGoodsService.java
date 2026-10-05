package edu.fafu.service.service.businessservice.managerservice;

import edu.fafu.database.dto.request.manager.UpdateManagerGoodsRequest;
import edu.fafu.database.entity.Result;

public interface ManagerGoodsService {
    Result<String> updateGoods(UpdateManagerGoodsRequest request);
}