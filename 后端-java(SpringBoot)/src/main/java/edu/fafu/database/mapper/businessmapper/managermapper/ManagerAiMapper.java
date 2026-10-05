package edu.fafu.database.mapper.businessmapper.managermapper;

import edu.fafu.database.entity.Ai;
import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ManagerAiMapper extends BaseMapper<Ai> {

    int updateList(@Param("list") List<Ai> list);

    default int update(Ai ai) {
        return updateList(List.of(ai));
    }
}
