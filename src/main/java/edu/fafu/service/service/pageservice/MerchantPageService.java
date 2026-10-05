package edu.fafu.service.service.pageservice;

import edu.fafu.database.entity.Result;
import edu.fafu.database.dto.request.page.*;
import edu.fafu.database.dto.response.merchant.*;
import edu.fafu.tool.page.PageResult;

public interface MerchantPageService {

    Result<PageResult<MerchantGoods>> pageMerchantGoods(PageMerchantGoodsRequest request, Integer userId);

    Result<PageResult<MerchantTrade>> pageMerchantTrade(PageMerchantTradeRequest request, Integer userId);

    Result<PageResult<MerchantUser>> pageMerchantUser(PageMerchantUserRequest request);

    Result<PageResult<MerchantBackground>> pageMerchantBackground(PageMerchantBackgroundRequest request, Integer userId);

    Result<PageResult<MerchantAi>> pageMerchantAi(PageMerchantAiRequest request, Integer userId);

    Result<MerchantBackgroundVO> merchantBackground(Integer userId, Integer currentUserId);
}