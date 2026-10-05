package edu.fafu.database.mapper.businessmapper.merchantmapper;

import edu.fafu.database.entity.Address;
import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MerchantAddressMapper extends BaseMapper<Address> {

    List<Address> selectList(@Param("entity") Address address);

    default Address selectById(Address address) {
        List<Address> list = selectList(address);
        return list.isEmpty() ? null : list.get(0);
    }
}
