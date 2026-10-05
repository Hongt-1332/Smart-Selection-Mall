package edu.fafu.database.mapper.businessmapper.managermapper;

import edu.fafu.database.entity.Address;
import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ManagerAddressMapper extends BaseMapper<Address> {

    int updateList(@Param("list") List<Address> list);

    default int update(Address address) {
        return updateList(List.of(address));
    }
}
