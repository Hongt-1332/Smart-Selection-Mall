package edu.fafu.database.mapper.businessmapper.usermapper;

import edu.fafu.database.entity.User;
import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserUserMapper extends BaseMapper<User> {

    int insertList(@Param("list") List<User> list);

    int updateList(@Param("list") List<User> list);

    List<User> selectList(@Param("entity") User user);

    List<User> selectByIds(@Param("ids") List<Integer> ids);

    default int insert(User user) {
        return insertList(List.of(user));
    }

    default int update(User user) {
        return updateList(List.of(user));
    }

    default User selectById(User user) {
        List<User> list = selectList(user);
        return list.isEmpty() ? null : list.get(0);
    }
}