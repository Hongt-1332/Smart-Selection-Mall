package edu.fafu.database.mapper.businessmapper.managermapper;

import edu.fafu.database.entity.Trade;
import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ManagerTradeMapper extends BaseMapper<Trade> {

    int updateList(@Param("list") List<Trade> list);

    default int update(Trade trade) {
        return updateList(List.of(trade));
    }
}
