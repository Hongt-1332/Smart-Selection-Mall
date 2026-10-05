package edu.fafu.service.service.businessservice.managerservice;

import edu.fafu.database.entity.Result;
import edu.fafu.database.entity.User;

public interface ManagerUserService {
    Result<String> updateUser(User user);
}