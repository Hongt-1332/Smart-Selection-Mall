package edu.fafu.service.service.businessservice.userservice;

import edu.fafu.database.entity.Result;

public interface UserTradeService {

    Result<String> cancelTrade(Integer id, Integer userId);
}