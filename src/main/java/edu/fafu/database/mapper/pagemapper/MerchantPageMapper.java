package edu.fafu.database.mapper.pagemapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import edu.fafu.database.dto.response.merchant.*;
import edu.fafu.database.dto.response.user.UserGoods;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MerchantPageMapper {
    IPage<MerchantGoods> selectGoodsList(Page<?> page, @Param("entity") MerchantGoods goods);
    IPage<MerchantTrade> selectTradeList(Page<?> page, @Param("entity") MerchantTrade trade);
    IPage<MerchantUser> selectMerchantUserList(Page<?> page, @Param("entity") MerchantUser user);
    IPage<MerchantAi> selectAiList(Page<?> page, @Param("entity") MerchantAi ai);
    IPage<MerchantBackground> selectBackgroundList(Page<?> page, @Param("entity") MerchantBackground background);

    int selectUnfinishedTradeCount(@Param("merchantUserId") Integer merchantUserId);

    MerchantBackgroundVO selectMerchantBackgroundInfo(@Param("userId") Integer userId);
    List<MerchantBackgroundVO.BackgroundImage> selectMerchantBackgroundImages(@Param("userId") Integer userId);
    List<UserGoods> selectMerchantLaunchGoods(@Param("userId") Integer userId, @Param("currentUserId") Integer currentUserId);
}