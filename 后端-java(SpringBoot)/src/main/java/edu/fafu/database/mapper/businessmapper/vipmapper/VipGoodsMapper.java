package edu.fafu.database.mapper.businessmapper.vipmapper;

import edu.fafu.database.entity.Goods;
import edu.fafu.database.dto.response.user.UserGoods;
import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface VipGoodsMapper extends BaseMapper<Goods> {

    List<UserGoods> selectByIds(@Param("ids") List<Integer> ids, @Param("userId") Integer userId);
}