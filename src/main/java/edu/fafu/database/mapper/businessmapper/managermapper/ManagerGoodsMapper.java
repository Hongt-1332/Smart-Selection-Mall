package edu.fafu.database.mapper.businessmapper.managermapper;

import edu.fafu.database.entity.Goods;
import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ManagerGoodsMapper extends BaseMapper<Goods> {

    Goods selectById(@Param("id") Integer id);

    int updateList(@Param("list") List<Goods> list);
}