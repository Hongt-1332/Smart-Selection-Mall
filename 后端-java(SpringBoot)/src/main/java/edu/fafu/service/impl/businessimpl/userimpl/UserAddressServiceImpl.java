package edu.fafu.service.impl.businessimpl.userimpl;

import edu.fafu.config.SystemConfig;
import edu.fafu.database.entity.Address;
import edu.fafu.database.entity.Result;
import edu.fafu.database.entity.User;
import edu.fafu.database.dto.request.user.AddAddressRequest;
import edu.fafu.database.dto.request.user.UpdateAddressRequest;
import edu.fafu.database.dto.response.user.UserAddress;
import edu.fafu.database.mapper.businessmapper.usermapper.UserAddressMapper;
import edu.fafu.database.mapper.businessmapper.usermapper.UserGoodsMapper;
import edu.fafu.database.mapper.businessmapper.usermapper.UserUserMapper;
import edu.fafu.exception.BusinessExceptionInterface;
import edu.fafu.service.service.businessservice.userservice.UserAddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserAddressServiceImpl implements UserAddressService, BusinessExceptionInterface {

    @Autowired
    private UserAddressMapper userAddressMapper;
    @Autowired
    private UserGoodsMapper userGoodsMapper;
    @Autowired
    private UserUserMapper userUserMapper;
    @Autowired
    private SystemConfig systemConfig;

    @Override
    public Result<List<UserAddress>> showUserAddress(Integer userId) {
        return null;
    }

    @Override
    public Result<String> addUserAddress(AddAddressRequest request, Integer userId) {
        User userQuery = new User();
        userQuery.setId(userId);
        User user = userUserMapper.selectById(userQuery);
        ensureNotNull(user, "用户不存在");

        Address existQuery = new Address();
        existQuery.setUserId(userId);
        existQuery.setDelete(false);
        List<Address> existList = userAddressMapper.selectList(existQuery);
        ensureTrue(existList.size() < systemConfig.getUserMaxAddresses(), "地址数量已达上限(" + systemConfig.getUserMaxAddresses() + ")");

        Address address = new Address();
        address.setUserId(userId);
        address.setCountry(request.getCountry());
        address.setProvince(request.getProvince());
        address.setCity(request.getCity());
        address.setCounty(request.getCounty());
        address.setDetail(request.getDetail());
        address.setCreateTime(LocalDateTime.now());
        address.setDelete(false);

        int nextAddressId = existList.stream()
                .mapToInt(a -> a.getAddressId() != null ? a.getAddressId() : 0)
                .max()
                .orElse(0) + 1;
        address.setAddressId(nextAddressId);

        userAddressMapper.insert(address);
        return Result.success("添加成功");
    }

    @Override
    public Result<String> updateUserAddress(UpdateAddressRequest request, Integer userId) {
        Address query = new Address();
        query.setId(request.getId());
        Address old = userAddressMapper.selectById(query);
        ensureNotNull(old, "地址不存在");
        ensureEquals(old.getUserId(), userId, "地址不属于当前用户");

        Address update = new Address();
        update.setId(request.getId());
        if (request.getCountry() != null) update.setCountry(request.getCountry());
        if (request.getProvince() != null) update.setProvince(request.getProvince());
        if (request.getCity() != null) update.setCity(request.getCity());
        if (request.getCounty() != null) update.setCounty(request.getCounty());
        if (request.getDetail() != null) update.setDetail(request.getDetail());

        userAddressMapper.update(update);
        return Result.success("修改成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> deleteUserAddress(Integer id, Integer userId) {
        Address query = new Address();
        query.setId(id);
        Address old = userAddressMapper.selectById(query);
        ensureNotNull(old, "地址不存在");
        ensureEquals(old.getUserId(), userId, "地址不属于当前用户");

        Address update = new Address();
        update.setId(id);
        update.setDelete(true);
        userAddressMapper.update(update);
        return Result.success("删除成功");
    }
}