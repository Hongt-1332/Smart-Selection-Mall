package edu.fafu.database.mapper.businessmapper.merchantmapper;

import edu.fafu.database.entity.Trade;
import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MerchantTradeMapper extends BaseMapper<Trade> {

    int updateList(@Param("list") List<Trade> list);

    List<Trade> selectList(@Param("entity") Trade trade);

    default int update(Trade trade) {
        return updateList(List.of(trade));
    }

    default Trade selectById(Trade trade) {
        List<Trade> list = selectList(trade);
        return list.isEmpty() ? null : list.get(0);
    }

    int countUnfinished(@Param("merchantUserId") Integer merchantUserId);
}