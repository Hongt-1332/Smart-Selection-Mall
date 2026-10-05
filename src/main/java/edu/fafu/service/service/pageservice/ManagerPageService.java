package edu.fafu.service.service.pageservice;

import edu.fafu.database.entity.Result;
import edu.fafu.database.dto.request.page.*;
import edu.fafu.database.dto.response.manager.*;
import edu.fafu.tool.page.PageResult;

public interface ManagerPageService {

    Result<PageResult<ManagerUser>> pageManagerUser(PageManagerUserRequest request);

    Result<PageResult<ManagerAddress>> pageManagerAddress(PageManagerAddressRequest request);

    Result<PageResult<ManagerCart>> pageManagerCart(PageManagerCartRequest request);

    Result<PageResult<ManagerGoods>> pageManagerGoods(PageManagerGoodsRequest request);

    Result<PageResult<ManagerTrade>> pageManagerTrade(PageManagerTradeRequest request);

    Result<PageResult<ManagerVipConfig>> pageManagerVipConfig(PageManagerVipConfigRequest request);

    Result<PageResult<ManagerVipTrade>> pageManagerVipTrade(PageManagerVipTradeRequest request);

    Result<PageResult<ManagerAi>> pageManagerAi(PageManagerAiRequest request);

    Result<PageResult<ManagerGoods>> pageManagerHistoryGoods(PageManagerHistoryGoodsRequest request);
}