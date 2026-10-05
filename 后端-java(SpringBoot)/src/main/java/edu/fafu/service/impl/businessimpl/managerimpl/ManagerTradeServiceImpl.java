package edu.fafu.service.impl.businessimpl.managerimpl;

import edu.fafu.database.entity.Result;
import edu.fafu.database.entity.Trade;
import edu.fafu.database.mapper.businessmapper.managermapper.ManagerTradeMapper;
import edu.fafu.exception.BusinessExceptionInterface;
import edu.fafu.service.service.businessservice.managerservice.ManagerTradeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ManagerTradeServiceImpl implements ManagerTradeService, BusinessExceptionInterface {

    @Autowired
    private ManagerTradeMapper managerTradeMapper;

    @Override
    public Result<String> updateTrade(Trade trade) {
        int result = managerTradeMapper.update(trade);
        ensureTrue(result > 0, "修改失败");
        return Result.success("修改成功");
    }
}