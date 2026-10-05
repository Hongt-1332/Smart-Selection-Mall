package edu.fafu.service.impl.businessimpl.managerimpl;

import edu.fafu.database.entity.Address;
import edu.fafu.database.entity.Result;
import edu.fafu.database.mapper.businessmapper.managermapper.ManagerAddressMapper;
import edu.fafu.exception.BusinessExceptionInterface;
import edu.fafu.service.service.businessservice.managerservice.ManagerAddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ManagerAddressServiceImpl implements ManagerAddressService, BusinessExceptionInterface {

    @Autowired
    private ManagerAddressMapper managerAddressMapper;

    @Override
    public Result<String> updateAddress(Address address) {
        int result = managerAddressMapper.update(address);
        ensureTrue(result > 0, "修改失败");
        return Result.success("修改成功");
    }
}