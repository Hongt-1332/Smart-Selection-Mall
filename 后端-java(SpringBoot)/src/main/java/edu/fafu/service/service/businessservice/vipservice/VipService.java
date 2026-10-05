package edu.fafu.service.service.businessservice.vipservice;

import edu.fafu.database.entity.Result;
import edu.fafu.database.entity.VipConfig;
import edu.fafu.database.dto.response.common.ImageResponse;
import edu.fafu.database.dto.request.vip.VipBuyRequest;
import edu.fafu.database.dto.response.vip.VipLevelResponse;
import edu.fafu.database.dto.response.user.UserGoods;

import java.util.List;

public interface VipService {

    Result<List<UserGoods>> vipGoods(Integer userId);

    Result<List<ImageResponse>> vipImage();

    Result<List<ImageResponse>> vipHomeImage();

    Result<VipLevelResponse> vipLevel(Integer userId, Integer vipLevel);

    Result<List<VipConfig>> vipConfig();

    Result<String> buyVip(VipBuyRequest request, Integer userId);
}