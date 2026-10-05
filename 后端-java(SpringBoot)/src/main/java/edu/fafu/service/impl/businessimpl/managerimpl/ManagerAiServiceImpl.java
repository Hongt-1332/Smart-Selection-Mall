package edu.fafu.service.impl.businessimpl.managerimpl;

import edu.fafu.database.entity.Ai;
import edu.fafu.database.entity.Result;
import edu.fafu.database.mapper.businessmapper.managermapper.ManagerAiMapper;
import edu.fafu.exception.BusinessExceptionInterface;
import edu.fafu.service.service.businessservice.managerservice.ManagerAiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ManagerAiServiceImpl implements ManagerAiService, BusinessExceptionInterface {

    @Autowired
    private ManagerAiMapper managerAiMapper;

    @Override
    public Result<String> updateAi(Ai ai) {
        int result = managerAiMapper.update(ai);
        ensureTrue(result > 0, "修改失败");
        return Result.success("修改成功");
    }
}