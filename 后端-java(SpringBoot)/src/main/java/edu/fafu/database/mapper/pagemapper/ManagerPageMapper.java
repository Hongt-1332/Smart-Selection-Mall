package edu.fafu.database.mapper.pagemapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import edu.fafu.database.dto.response.manager.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ManagerPageMapper {
    IPage<ManagerUser> selectUserList(Page<?> page, @Param("entity") ManagerUser user);
    IPage<ManagerAddress> selectAddressList(Page<?> page, @Param("entity") ManagerAddress address);
    IPage<ManagerCart> selectCartList(Page<?> page, @Param("entity") ManagerCart cart);
    IPage<ManagerGoods> selectGoodsList(Page<?> page, @Param("entity") ManagerGoods goods);
    IPage<ManagerGoods> selectGoodsChain(Page<?> page, @Param("goodsId") String goodsId);
    IPage<ManagerTrade> selectTradeList(Page<?> page, @Param("entity") ManagerTrade trade);
    IPage<ManagerVipConfig> selectVipConfigList(Page<?> page, @Param("entity") ManagerVipConfig vipConfig);
    IPage<ManagerVipTrade> selectVipTradeList(Page<?> page, @Param("entity") ManagerVipTrade vipTrade);
    IPage<ManagerAi> selectAiList(Page<?> page, @Param("entity") ManagerAi ai);
}