package edu.fafu.service.impl.businessimpl.merchantimpl;

import edu.fafu.config.VipConfigCache;
import edu.fafu.database.entity.Ai;
import edu.fafu.database.entity.Goods;
import edu.fafu.database.entity.Result;
import edu.fafu.database.entity.User;
import edu.fafu.database.dto.request.merchant.AddAiRequest;
import edu.fafu.database.dto.request.merchant.UpdateAiRequest;
import edu.fafu.database.mapper.businessmapper.merchantmapper.AiMapper;
import edu.fafu.database.mapper.businessmapper.merchantmapper.MerchantGoodsMapper;
import edu.fafu.database.mapper.businessmapper.usermapper.UserUserMapper;
import edu.fafu.exception.BusinessExceptionInterface;
import edu.fafu.service.service.businessservice.merchantservice.MerchantAiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MerchantAiServiceImpl implements MerchantAiService, BusinessExceptionInterface {

    @Autowired
    private AiMapper aiMapper;
    @Autowired
    private MerchantGoodsMapper merchantGoodsMapper;
    @Autowired
    private UserUserMapper userUserMapper;
    @Autowired
    private VipConfigCache vipConfigCache;

    @Override
    public Result<String> addAi(AddAiRequest request, Integer userId) {
        User userQuery = new User();
        userQuery.setId(userId);
        User user = userUserMapper.selectById(userQuery);
        ensureNotNull(user, "用户不存在");

        Ai ai = new Ai();
        ai.setUserId(userId);
        ai.setCategory(request.getCategory());
        ai.setKind(request.getKind());
        ai.setName(request.getName());
        ai.setPrice(request.getPrice());
        ai.setSimpleDescription(request.getSimpleDescription());
        ai.setFeatures(request.getFeatures());
        ai.setCreateTime(LocalDateTime.now());
        ai.setDelete(false);

        aiMapper.insert(ai);
        return Result.success("添加成功");
    }

    @Override
    public Result<String> updateAi(UpdateAiRequest request, Integer userId) {
        Ai old = aiMapper.selectById(request.getId());
        ensureNotNull(old, "AI不存在");
        ensureEquals(old.getUserId(), userId, "AI不属于当前用户");

        Ai update = new Ai();
        update.setId(request.getId());
        if (request.getCategory() != null) update.setCategory(request.getCategory());
        if (request.getKind() != null) update.setKind(request.getKind());
        if (request.getName() != null) update.setName(request.getName());
        if (request.getPrice() != null) update.setPrice(request.getPrice());
        if (request.getSimpleDescription() != null) update.setSimpleDescription(request.getSimpleDescription());
        if (request.getFeatures() != null) update.setFeatures(request.getFeatures());

        aiMapper.update(update);
        return Result.success("修改成功");
    }

    @Override
    public Result<String> deleteAi(Integer id, Integer userId) {
        Ai old = aiMapper.selectById(id);
        ensureNotNull(old, "AI不存在");
        ensureEquals(old.getUserId(), userId, "AI不属于当前用户");

        Ai update = new Ai();
        update.setId(id);
        update.setDelete(true);
        aiMapper.update(update);
        return Result.success("删除成功");
    }

    @Override
    public Result<List<Goods>> goodsList(Integer userId) {
        return null;
    }
}