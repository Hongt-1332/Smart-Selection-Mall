package edu.fafu.service.service.businessservice.merchantservice;

import edu.fafu.database.entity.Result;
import edu.fafu.database.entity.Trade;

public interface MerchantTradeService {

    Result<String> cancelTrade(Integer id, Integer userId);

    Result<String> finishTrade(Integer id, Integer userId);

    Result<Integer> getUnfinishedCount(Integer userId);

    Result<Trade> getTrade(Integer id, Integer userId);

    Result<String> deliverTrade(Integer id, Integer userId);

    Result<String> confirmTrade(Integer id, Integer userId);

    Result<String> deleteTrade(Integer id, Integer userId);
}