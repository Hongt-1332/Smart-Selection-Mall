package edu.fafu.database.mapper.pagemapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import edu.fafu.database.dto.response.user.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserPageMapper {

    List<UserUser> selectUserList(@Param("entity") UserUser user);
    IPage<UserAddress> selectAddressList(Page<?> page, @Param("entity") UserAddress address);
    IPage<UserCart> selectCartList(Page<?> page, @Param("entity") UserCart cart);
    IPage<UserGoods> selectGoodsList(Page<?> page, @Param("entity") UserGoods goods);
    IPage<UserTrade> selectTradeList(Page<?> page, @Param("entity") UserTrade trade);
    IPage<UserBackground> selectBackgroundList(Page<?> page, @Param("entity") UserBackground background);
    IPage<UserVipConfig> selectVipConfigList(Page<?> page, @Param("entity") UserVipConfig vipConfig);
    IPage<UserVipTrade> selectVipTradeList(Page<?> page, @Param("entity") UserVipTrade vipTrade);

    int selectCartCount(@Param("userId") Integer userId);

    List<UserGoods> selectGoodsByIds(@Param("ids") List<Integer> ids, @Param("userId") Integer userId);

    UserBackgroundVO selectUserBackgroundInfo(@Param("userId") Integer userId);
    List<UserBackgroundVO.BackgroundImage> selectUserBackgroundImages(@Param("userId") Integer userId);
    List<UserBackgroundVO.UserBackgroundGoods> selectUserBackgroundGoods(@Param("userId") Integer userId);
}