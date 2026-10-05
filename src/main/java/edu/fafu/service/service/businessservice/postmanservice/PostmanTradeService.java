package edu.fafu.service.service.businessservice.postmanservice;

import edu.fafu.database.entity.Result;
import edu.fafu.database.entity.Trade;

public interface PostmanTradeService {

    Result<String> updateCurrentAddress(Trade trade, Integer userId);

    Result<String> deliverTrade(Integer id, Integer userId);

    Result<String> confirmTrade(Integer id, Integer userId);

    Result<String> deleteTrade(Integer id, Integer userId);
}