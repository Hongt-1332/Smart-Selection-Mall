package edu.fafu.database.mapper.businessmapper.merchantmapper;

import edu.fafu.database.entity.Goods;
import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MerchantGoodsMapper extends BaseMapper<Goods> {

    int insertList(@Param("list") List<Goods> list);

    int updateList(@Param("list") List<Goods> list);

    List<Goods> selectList(@Param("entity") Goods goods);

    default int insert(Goods goods) {
        return insertList(List.of(goods));
    }

    default int update(Goods goods) {
        return updateList(List.of(goods));
    }

    default Goods selectById(Goods goods) {
        List<Goods> list = selectList(goods);
        return list.isEmpty() ? null : list.get(0);
    }
}
