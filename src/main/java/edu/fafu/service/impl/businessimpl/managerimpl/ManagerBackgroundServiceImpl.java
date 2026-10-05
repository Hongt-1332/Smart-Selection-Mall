package edu.fafu.service.impl.businessimpl.managerimpl;

import edu.fafu.database.dto.request.manager.UpdateManagerBackgroundRequest;
import edu.fafu.database.entity.Background;
import edu.fafu.database.entity.Result;
import edu.fafu.database.mapper.businessmapper.managermapper.ManagerBackgroundMapper;
import edu.fafu.exception.BusinessExceptionInterface;
import edu.fafu.service.service.businessservice.managerservice.ManagerBackgroundService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ManagerBackgroundServiceImpl implements ManagerBackgroundService, BusinessExceptionInterface {

    @Autowired
    private ManagerBackgroundMapper managerBackgroundMapper;

    @Override
    public Result<String> updateBackground(UpdateManagerBackgroundRequest request) {
        Background background = new Background();
        background.setId(request.getId());
        if (request.getImagePath() != null) background.setImagePath(request.getImagePath());
        if (request.getCreateTime() != null) background.setCreateTime(request.getCreateTime());
        int result = managerBackgroundMapper.update(background);
        ensureTrue(result > 0, "修改失败");
        return Result.success("修改成功");
    }
}