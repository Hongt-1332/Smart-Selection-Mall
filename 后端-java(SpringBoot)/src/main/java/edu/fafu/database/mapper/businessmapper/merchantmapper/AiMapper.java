package edu.fafu.database.mapper.businessmapper.merchantmapper;

import edu.fafu.database.entity.Ai;
import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AiMapper extends BaseMapper<Ai> {

    int insertList(@Param("list") List<Ai> list);

    int updateList(@Param("list") List<Ai> list);

    List<Ai> selectList(@Param("entity") Ai ai);

    default int insert(Ai ai) {
        return insertList(List.of(ai));
    }

    default int update(Ai ai) {
        return updateList(List.of(ai));
    }

    default Ai selectById(Ai ai) {
        List<Ai> list = selectList(ai);
        return list.isEmpty() ? null : list.get(0);
    }
}