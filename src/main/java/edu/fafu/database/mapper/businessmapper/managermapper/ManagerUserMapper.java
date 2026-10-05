package edu.fafu.database.mapper.businessmapper.managermapper;

import edu.fafu.database.entity.User;
import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ManagerUserMapper extends BaseMapper<User> {

    int updateList(@Param("list") List<User> list);

    default int update(User user) {
        return updateList(List.of(user));
    }
}