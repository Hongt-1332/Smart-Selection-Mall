package edu.fafu.service.service.businessservice.merchantservice;

import edu.fafu.database.entity.Goods;
import edu.fafu.database.entity.Result;
import edu.fafu.database.dto.request.merchant.AddAiRequest;
import edu.fafu.database.dto.request.merchant.UpdateAiRequest;

import java.util.List;

public interface MerchantAiService {

    Result<String> addAi(AddAiRequest request, Integer userId);

    Result<String> updateAi(UpdateAiRequest request, Integer userId);

    Result<String> deleteAi(Integer id, Integer userId);

    Result<List<Goods>> goodsList(Integer userId);
}