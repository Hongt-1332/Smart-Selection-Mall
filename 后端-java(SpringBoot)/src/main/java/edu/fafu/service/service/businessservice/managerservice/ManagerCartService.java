package edu.fafu.service.service.businessservice.managerservice;

import edu.fafu.database.entity.Result;

public interface ManagerCartService {
    Result<String> deleteCart(Integer id);
}