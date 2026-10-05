package edu.fafu.database.mapper.businessmapper.usermapper;

import edu.fafu.database.entity.Address;
import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserAddressMapper extends BaseMapper<Address> {

    int insertList(@Param("list") List<Address> list);

    int deleteList(@Param("list") List<Address> list);

    int updateList(@Param("list") List<Address> list);

    List<Address> selectList(@Param("entity") Address address);

    List<Address> selectByIds(@Param("ids") List<Integer> ids);

    default int insert(Address address) {
        return insertList(List.of(address));
    }

    default int delete(Address address) {
        return deleteList(List.of(address));
    }

    default int update(Address address) {
        return updateList(List.of(address));
    }

    default Address selectById(Address address) {
        List<Address> list = selectList(address);
        return list.isEmpty() ? null : list.get(0);
    }
}