package edu.fafu.service.service.businessservice.managerservice;

import edu.fafu.database.entity.Result;
import edu.fafu.database.entity.Trade;

public interface ManagerTradeService {
    Result<String> updateTrade(Trade trade);
}