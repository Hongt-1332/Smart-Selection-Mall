package edu.fafu.service.service.businessservice.managerservice;

import edu.fafu.database.entity.Address;
import edu.fafu.database.entity.Result;

public interface ManagerAddressService {
    Result<String> updateAddress(Address address);
}