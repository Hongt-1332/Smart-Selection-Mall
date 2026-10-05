package edu.fafu.database.mapper.businessmapper.merchantmapper;

import edu.fafu.database.entity.User;
import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MerchantUserMapper extends BaseMapper<User> {

    List<User> selectList(@Param("entity") User user);

    default User selectById(User user) {
        List<User> list = selectList(user);
        return list.isEmpty() ? null : list.get(0);
    }
}
