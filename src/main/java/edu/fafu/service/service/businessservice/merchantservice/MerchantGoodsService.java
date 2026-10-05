package edu.fafu.service.service.businessservice.merchantservice;

import edu.fafu.database.entity.Result;
import edu.fafu.database.dto.request.merchant.AddMerchantGoodsRequest;
import edu.fafu.database.dto.request.merchant.UpdateMerchantGoodsRequest;
import edu.fafu.database.dto.response.merchant.MerchantGoods;
import org.springframework.web.multipart.MultipartFile;

public interface MerchantGoodsService {

    Result<String> addGoods(AddMerchantGoodsRequest request, MultipartFile file, Integer userId);

    Result<String> updateImage(Integer id, MultipartFile file, Integer userId);

    Result<String> updateGoods(UpdateMerchantGoodsRequest request, Integer userId);

    Result<String> updateDescribe(Integer id, String describe, Integer userId);

    Result<String> updateLaunch(Integer id, Boolean launch, Integer userId);

    Result<String> deleteGoods(Integer id, Integer userId);

    Result<MerchantGoods> getGoods(Integer id, Integer userId);

    Result<Integer> getGoodsCount(Integer userId);
}