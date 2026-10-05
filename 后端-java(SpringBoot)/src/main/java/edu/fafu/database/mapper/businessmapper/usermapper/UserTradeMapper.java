package edu.fafu.database.mapper.businessmapper.usermapper;

import edu.fafu.database.entity.Trade;
import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserTradeMapper extends BaseMapper<Trade> {

    int insertList(@Param("list") List<Trade> list);

    int updateList(@Param("list") List<Trade> list);

    List<Trade> selectList(@Param("entity") Trade trade);

    default int insert(Trade trade) {
        return insertList(List.of(trade));
    }

    default int update(Trade trade) {
        return updateList(List.of(trade));
    }

    default Trade selectById(Trade trade) {
        List<Trade> list = selectList(trade);
        return list.isEmpty() ? null : list.get(0);
    }
}