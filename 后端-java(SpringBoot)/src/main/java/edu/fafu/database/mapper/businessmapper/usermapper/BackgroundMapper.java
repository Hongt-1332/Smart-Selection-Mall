package edu.fafu.database.mapper.businessmapper.usermapper;

import edu.fafu.database.entity.Background;
import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BackgroundMapper extends BaseMapper<Background> {

    int insertList(@Param("list") List<Background> list);

    int updateList(@Param("list") List<Background> list);

    List<Background> selectList(@Param("entity") Background background);

    int deleteList(@Param("ids") List<Integer> ids);

    default int insert(Background background) {
        return insertList(List.of(background));
    }

    default int update(Background background) {
        return updateList(List.of(background));
    }

    default Background selectById(Background background) {
        List<Background> list = selectList(background);
        return list.isEmpty() ? null : list.get(0);
    }
}