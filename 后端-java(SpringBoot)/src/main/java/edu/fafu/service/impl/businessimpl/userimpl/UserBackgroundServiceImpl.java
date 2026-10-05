package edu.fafu.service.impl.businessimpl.userimpl;

import edu.fafu.config.SystemConfig;
import edu.fafu.config.VipConfigCache;
import edu.fafu.database.dto.request.user.AddBackgroundRequest;
import edu.fafu.database.dto.request.user.PaymentActionRequest;
import edu.fafu.database.dto.request.user.UpdateBackgroundRequest;
import edu.fafu.database.entity.Background;
import edu.fafu.database.entity.Result;
import edu.fafu.database.entity.User;
import edu.fafu.database.mapper.businessmapper.usermapper.BackgroundMapper;
import edu.fafu.database.mapper.businessmapper.usermapper.UserUserMapper;
import edu.fafu.exception.BusinessExceptionInterface;
import edu.fafu.service.service.businessservice.userservice.UserBackgroundService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

@Service
public class UserBackgroundServiceImpl implements UserBackgroundService, BusinessExceptionInterface {

    @Autowired
    private BackgroundMapper backgroundMapper;
    @Autowired
    private UserUserMapper userUserMapper;
    @Autowired
    private VipConfigCache vipConfigCache;
    @Autowired
    private SystemConfig systemConfig;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> addBackground(AddBackgroundRequest request, Integer userId) {

        User userQuery = new User();
        userQuery.setId(userId);
        User user = userUserMapper.selectById(userQuery);
        ensureNotNull(user, "用户不存在");

        int level = user.getLevel() != null ? user.getLevel() : 0;
        int monthlyLimit = vipConfigCache.getMonthlyUpdateBackground(level);
        int currentCount = resetMonthlyCountIfNeeded(user);

        ensureTrue(currentCount < monthlyLimit, "本月背景更新次数已达上限(" + monthlyLimit + "次)");

        Background existQuery = new Background();
        existQuery.setUserId(userId);
        List<Background> existList = backgroundMapper.selectList(existQuery);
        ensureTrue(existList.size() < systemConfig.getUserMaxBackgrounds(), "背景数量已达上限");

        Background background = new Background();
        background.setUserId(userId);
        background.setImagePath(request.getImagePath());
        background.setCreateTime(LocalDateTime.now());
        if (request.getSequence() == null) {
            background.setSequence(existList.stream()
                    .mapToInt(b -> b.getSequence() != null ? b.getSequence() : 0)
                    .max()
                    .orElse(0) + 1);
        } else {
            background.setSequence(request.getSequence());
        }

        backgroundMapper.insert(background);

        User update = new User();
        update.setId(userId);
        update.setUploadBackground(currentCount + 1);
        update.setUploadTime(LocalDateTime.now());
        userUserMapper.update(update);

        return Result.success("添加成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> updateBackground(UpdateBackgroundRequest request, Integer userId) {

        Background query = new Background();
        query.setId(request.getId());
        Background old = backgroundMapper.selectById(query);
        ensureNotNull(old, "背景不存在");
        ensureEquals(old.getUserId(), userId, "背景不属于当前用户");

        User userQuery = new User();
        userQuery.setId(userId);
        User user = userUserMapper.selectById(userQuery);
        int level = user != null && user.getLevel() != null ? user.getLevel() : 0;
        int monthlyLimit = vipConfigCache.getMonthlyUpdateBackground(level);
        int currentCount = resetMonthlyCountIfNeeded(user);

        ensureTrue(currentCount < monthlyLimit, "本月背景更新次数已达上限(" + monthlyLimit + "次)");

        Background update = new Background();
        update.setId(request.getId());
        if (request.getImagePath() != null) update.setImagePath(request.getImagePath());
        if (request.getSequence() != null) update.setSequence(request.getSequence());

        backgroundMapper.update(update);

        User userUpdate = new User();
        userUpdate.setId(userId);
        userUpdate.setUploadBackground(currentCount + 1);
        userUpdate.setUploadTime(LocalDateTime.now());
        userUserMapper.update(userUpdate);

        return Result.success("修改成功");
    }

    @Override
    public Result<String> pay(PaymentActionRequest request, Integer userId) {
        Background query = new Background();
        query.setId(request.getId());
        Background bg = backgroundMapper.selectById(query);
        ensureNotNull(bg, "背景不存在");
        ensureEquals(bg.getUserId(), userId, "背景不属于当前用户");

        return Result.success("支付成功");
    }

    private int resetMonthlyCountIfNeeded(User user) {
        int count = user.getUploadBackground() != null ? user.getUploadBackground() : 0;
        if (user.getUploadTime() == null || !YearMonth.from(user.getUploadTime()).equals(YearMonth.now())) {
            count = 0;
            User reset = new User();
            reset.setId(user.getId());
            reset.setUpdateAvatar(0);
            reset.setUploadBackground(0);
            reset.setUploadGoods(0);
            reset.setUploadTime(LocalDateTime.now());
            userUserMapper.update(reset);
        }
        return count;
    }
}