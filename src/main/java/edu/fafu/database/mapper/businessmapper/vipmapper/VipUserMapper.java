package edu.fafu.database.mapper.businessmapper.vipmapper;

import edu.fafu.database.entity.User;
import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface VipUserMapper extends BaseMapper<User> {

    int updateList(@Param("list") List<User> list);

    List<User> selectList(@Param("entity") User user);

    default int update(User user) {
        return updateList(List.of(user));
    }

    default User selectById(User user) {
        List<User> list = selectList(user);
        return list.isEmpty() ? null : list.get(0);
    }
}