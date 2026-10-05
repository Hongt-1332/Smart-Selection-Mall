package edu.fafu.service.impl.businessimpl.managerimpl;

import edu.fafu.database.entity.Result;
import edu.fafu.database.entity.VipTrade;
import edu.fafu.database.mapper.businessmapper.vipmapper.VipTradeMapper;
import edu.fafu.exception.BusinessExceptionInterface;
import edu.fafu.service.service.businessservice.managerservice.ManagerVipTradeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ManagerVipTradeServiceImpl implements ManagerVipTradeService, BusinessExceptionInterface {

    @Autowired
    private VipTradeMapper vipTradeMapper;

    @Override
    public Result<String> updateVipTrade(VipTrade vipTrade) {
        int result = vipTradeMapper.updateById(vipTrade);
        ensureTrue(result > 0, "修改失败");
        return Result.success("修改成功");
    }
}