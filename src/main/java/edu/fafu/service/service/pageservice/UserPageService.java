package edu.fafu.service.service.pageservice;

import edu.fafu.database.entity.Result;
import edu.fafu.database.dto.request.page.*;
import edu.fafu.database.dto.response.user.*;
import edu.fafu.tool.page.PageResult;

public interface UserPageService {

    Result<UserUser> pageUserUser(Integer userId);

    Result<PageResult<UserAddress>> pageUserAddress(PageUserAddressRequest request, Integer userId);

    Result<PageResult<UserCart>> pageUserCart(PageUserCartRequest request, Integer userId);

    Result<PageResult<UserGoods>> pageUserGoods(PageUserGoodsRequest request, Integer userId);

    Result<PageResult<UserTrade>> pageUserTrade(PageUserTradeRequest request, Integer userId);

    Result<PageResult<UserBackground>> pageUserBackground(PageUserBackgroundRequest request, Integer userId);

    Result<PageResult<UserVipConfig>> pageUserVipConfig(PageUserVipConfigRequest request);

    Result<PageResult<UserVipTrade>> pageUserVipTrade(PageUserVipTradeRequest request, Integer userId);

    Result<UserBackgroundVO> userBackground(Integer userId);
}