package edu.fafu.database.mapper.businessmapper.managermapper;

import edu.fafu.database.entity.Cart;
import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ManagerCartMapper extends BaseMapper<Cart> {

    int deleteList(@Param("list") List<Cart> list);

    default int delete(Cart cart) {
        return deleteList(List.of(cart));
    }
}
