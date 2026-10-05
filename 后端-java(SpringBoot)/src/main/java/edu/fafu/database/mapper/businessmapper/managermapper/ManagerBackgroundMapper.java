package edu.fafu.database.mapper.businessmapper.managermapper;

import edu.fafu.database.entity.Background;
import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ManagerBackgroundMapper extends BaseMapper<Background> {

    int updateList(@Param("list") List<Background> list);

    default int update(Background background) {
        return updateList(List.of(background));
    }
}
