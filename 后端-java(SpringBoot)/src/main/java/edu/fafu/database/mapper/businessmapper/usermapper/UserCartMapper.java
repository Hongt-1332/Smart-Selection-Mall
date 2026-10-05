package edu.fafu.database.mapper.businessmapper.usermapper;

import edu.fafu.database.entity.Cart;
import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserCartMapper extends BaseMapper<Cart> {

    int insertList(@Param("list") List<Cart> list);

    int deleteList(@Param("list") List<Cart> list);

    int updateList(@Param("list") List<Cart> list);

    List<Cart> selectList(@Param("entity") Cart cart);

    List<Cart> selectByIds(@Param("ids") List<Integer> ids);

    default int insert(Cart cart) {
        return insertList(List.of(cart));
    }

    default int delete(Cart cart) {
        return deleteList(List.of(cart));
    }

    default int update(Cart cart) {
        return updateList(List.of(cart));
    }

    default Cart selectById(Cart cart) {
        List<Cart> list = selectList(cart);
        return list.isEmpty() ? null : list.get(0);
    }

    int countByUserId(@Param("userId") Integer userId);
}