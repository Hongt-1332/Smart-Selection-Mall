package edu.fafu.service.service.businessservice.userservice;

import edu.fafu.database.entity.Result;
import edu.fafu.database.dto.request.user.AddAddressRequest;
import edu.fafu.database.dto.request.user.UpdateAddressRequest;
import edu.fafu.database.dto.response.user.UserAddress;

import java.util.List;

public interface UserAddressService {

    Result<List<UserAddress>> showUserAddress(Integer userId);

    Result<String> addUserAddress(AddAddressRequest request, Integer userId);

    Result<String> updateUserAddress(UpdateAddressRequest request, Integer userId);

    Result<String> deleteUserAddress(Integer id, Integer userId);
}