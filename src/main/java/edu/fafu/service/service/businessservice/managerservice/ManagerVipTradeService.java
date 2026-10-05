package edu.fafu.service.service.businessservice.managerservice;

import edu.fafu.database.entity.Result;
import edu.fafu.database.entity.VipTrade;

public interface ManagerVipTradeService {
    Result<String> updateVipTrade(VipTrade vipTrade);
}