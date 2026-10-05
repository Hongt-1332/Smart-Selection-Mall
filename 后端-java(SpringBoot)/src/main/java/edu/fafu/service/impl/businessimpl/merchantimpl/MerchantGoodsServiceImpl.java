package edu.fafu.service.impl.businessimpl.merchantimpl;

import edu.fafu.config.SystemConfig;
import edu.fafu.config.VipConfigCache;
import edu.fafu.database.entity.Address;
import edu.fafu.database.entity.Goods;
import edu.fafu.database.entity.Result;
import edu.fafu.database.entity.User;
import edu.fafu.database.dto.request.merchant.AddMerchantGoodsRequest;
import edu.fafu.database.dto.request.merchant.UpdateMerchantGoodsRequest;
import edu.fafu.database.dto.response.merchant.MerchantGoods;
import edu.fafu.database.mapper.businessmapper.merchantmapper.MerchantAddressMapper;
import edu.fafu.database.mapper.businessmapper.merchantmapper.MerchantGoodsMapper;
import edu.fafu.database.mapper.businessmapper.merchantmapper.MerchantUserMapper;
import edu.fafu.database.mapper.businessmapper.usermapper.UserUserMapper;
import edu.fafu.exception.BusinessException;
import edu.fafu.exception.BusinessExceptionInterface;
import edu.fafu.service.service.businessservice.merchantservice.MerchantGoodsService;
import edu.fafu.tool.image.ImageUtil;
import edu.fafu.tool.oss.OssUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

@Service
public class MerchantGoodsServiceImpl implements MerchantGoodsService, BusinessExceptionInterface {

    @Autowired
    private MerchantGoodsMapper merchantGoodsMapper;
    @Autowired
    private MerchantAddressMapper merchantAddressMapper;
    @Autowired
    private MerchantUserMapper merchantUserMapper;
    @Autowired
    private VipConfigCache vipConfigCache;
    @Autowired
    private UserUserMapper userUserMapper;
    @Autowired
    private OssUtil ossUtil;
    @Autowired
    private SystemConfig systemConfig;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> addGoods(AddMerchantGoodsRequest request, MultipartFile file, Integer userId) {
        Address addressQuery = new Address();
        addressQuery.setId(request.getAddressId());
        Address address = merchantAddressMapper.selectById(addressQuery);
        ensureNotNull(address, "未设置默认地址");

        User levelQuery = new User();
        levelQuery.setId(userId);
        User currentUser = merchantUserMapper.selectById(levelQuery);
        int level = currentUser != null && currentUser.getLevel() != null ? currentUser.getLevel() : 0;
        int goodsMax = vipConfigCache.getMaxGoodsQuantity(level);

        Goods countQuery = new Goods();
        countQuery.setUserId(userId);
        countQuery.setDelete(false);
        List<Goods> userGoods = merchantGoodsMapper.selectList(countQuery);
        ensureTrue(userGoods.size() < goodsMax, "商品上架数量已达上限(" + goodsMax + "个)");

        int monthlyLimit = vipConfigCache.getMonthlyUpdateGoods(level);
        int currentCount = resetMonthlyCountIfNeeded(currentUser);
        ensureTrue(currentCount < monthlyLimit, "本月商品更新次数已达上限(" + monthlyLimit + "次)");

        Goods checkName = new Goods();
        checkName.setUserId(userId);
        checkName.setGoodsName(request.getGoodsName());
        checkName.setDelete(false);
        List<Goods> existList = merchantGoodsMapper.selectList(checkName);
        ensureEmpty(existList, "商品名称已存在");

        ensureTrue(request.getGoodsPrice().compareTo(systemConfig.getGoodsMinPrice()) >= 0, "商品单价不能小于" + systemConfig.getGoodsMinPrice());

        String goodsId = UUID.randomUUID().toString().replace("-", "");
        String ossKey = userId + "/" + goodsId + ".webp";
        String imageUrl = ImageUtil.saveImageToOss(systemConfig, ossUtil, file, ossKey);

        Goods goods = new Goods();
        goods.setUserId(userId);
        goods.setAddressId(request.getAddressId());
        goods.setGoodsName(request.getGoodsName());
        goods.setDescribe(request.getDescribe());
        goods.setGoodsPrice(request.getGoodsPrice());
        goods.setGoodsStock(request.getGoodsStock());
        goods.setGoodsId(goodsId);
        goods.setImagePath(imageUrl);
        goods.setCreateTime(LocalDateTime.now());
        goods.setLaunch(false);
        goods.setDelete(false);

        merchantGoodsMapper.insert(goods);

        User countUpdate = new User();
        countUpdate.setId(userId);
        countUpdate.setUploadGoods(currentCount + 1);
        countUpdate.setUploadTime(LocalDateTime.now());
        userUserMapper.update(countUpdate);

        return Result.success("添加成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> updateGoods(UpdateMerchantGoodsRequest request, Integer userId) {
        Goods exist = new Goods();
        exist.setId(request.getId());
        Goods old = merchantGoodsMapper.selectById(exist);
        ensureNotNull(old, "商品不存在");

        ensureEquals(old.getUserId(), userId, "商品不属于当前商户");

        ensureTrue(request.getGoodsPrice() == null || request.getGoodsPrice().compareTo(systemConfig.getGoodsMinPrice()) >= 0, "商品单价不能小于" + systemConfig.getGoodsMinPrice());

        String newGoodsName = request.getGoodsName() != null ? request.getGoodsName() : old.getGoodsName();
        if (!newGoodsName.equals(old.getGoodsName())) {
            Goods checkName = new Goods();
            checkName.setUserId(userId);
            checkName.setGoodsName(newGoodsName);
            checkName.setDelete(false);
            List<Goods> existList = merchantGoodsMapper.selectList(checkName);
            ensureEmpty(existList, "商品名称已存在");
        }

        User levelQuery = new User();
        levelQuery.setId(userId);
        User currentUser = merchantUserMapper.selectById(levelQuery);
        int level = currentUser != null && currentUser.getLevel() != null ? currentUser.getLevel() : 0;
        int monthlyLimit = vipConfigCache.getMonthlyUpdateGoods(level);
        int currentCount = resetMonthlyCountIfNeeded(currentUser);
        ensureTrue(currentCount < monthlyLimit, "本月商品更新次数已达上限(" + monthlyLimit + "次)");

        Goods newGoods = new Goods();
        newGoods.setUserId(userId);
        newGoods.setGoodsId(UUID.randomUUID().toString().replace("-", ""));
        newGoods.setAddressId(old.getAddressId());
        newGoods.setGoodsName(request.getGoodsName() != null ? request.getGoodsName() : old.getGoodsName());
        newGoods.setDescribe(request.getDescribe() != null ? request.getDescribe() : old.getDescribe());
        newGoods.setGoodsPrice(request.getGoodsPrice() != null ? request.getGoodsPrice() : old.getGoodsPrice());
        newGoods.setGoodsStock(request.getGoodsStock() != null ? request.getGoodsStock() : old.getGoodsStock());
        newGoods.setImagePath(old.getImagePath());
        newGoods.setLaunch(request.getLaunch() != null ? request.getLaunch() : old.getLaunch());
        newGoods.setPreId(old.getId());
        newGoods.setCreateTime(LocalDateTime.now());
        newGoods.setDelete(false);

        merchantGoodsMapper.insert(newGoods);

        Goods offOld = new Goods();
        offOld.setId(old.getId());
        offOld.setDelete(true);
        offOld.setLaunch(false);
        merchantGoodsMapper.update(offOld);

        User countUpdate = new User();
        countUpdate.setId(userId);
        countUpdate.setUploadGoods(currentCount + 1);
        countUpdate.setUploadTime(LocalDateTime.now());
        userUserMapper.update(countUpdate);

        return Result.success("修改成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> updateImage(Integer id, MultipartFile file, Integer userId) {
        Goods exist = new Goods();
        exist.setId(id);
        Goods goods = merchantGoodsMapper.selectById(exist);
        ensureNotNull(goods, "商品不存在");

        ensureEquals(goods.getUserId(), userId, "商品不属于当前商户");

        User levelQuery = new User();
        levelQuery.setId(userId);
        User currentUser = merchantUserMapper.selectById(levelQuery);
        int level = currentUser != null && currentUser.getLevel() != null ? currentUser.getLevel() : 0;
        int monthlyLimit = vipConfigCache.getMonthlyUpdateGoods(level);
        int currentCount = resetMonthlyCountIfNeeded(currentUser);
        ensureTrue(currentCount < monthlyLimit, "本月商品更新次数已达上限(" + monthlyLimit + "次)");

        String ossKey = userId + "/" + UUID.randomUUID().toString().replace("-", "") + ".webp";
        String imageUrl = ImageUtil.saveImageToOss(systemConfig, ossUtil, file, ossKey);

        Goods update = new Goods();
        update.setId(id);
        update.setImagePath(imageUrl);
        merchantGoodsMapper.update(update);

        User countUpdate = new User();
        countUpdate.setId(userId);
        countUpdate.setUploadGoods(currentCount + 1);
        countUpdate.setUploadTime(LocalDateTime.now());
        userUserMapper.update(countUpdate);

        return Result.success("修改成功");
    }

    @Override
    public Result<String> updateDescribe(Integer id, String describe, Integer userId) {
        Goods exist = new Goods();
        exist.setId(id);
        Goods goods = merchantGoodsMapper.selectById(exist);
        ensureNotNull(goods, "商品不存在");

        ensureEquals(goods.getUserId(), userId, "商品不属于当前商户");

        Goods update = new Goods();
        update.setId(id);
        update.setDescribe(describe);
        merchantGoodsMapper.update(update);
        return Result.success("修改成功");
    }

    @Override
    public Result<String> updateLaunch(Integer id, Boolean launch, Integer userId) {
        Goods exist = new Goods();
        exist.setId(id);
        Goods goods = merchantGoodsMapper.selectById(exist);
        ensureNotNull(goods, "商品不存在");

        ensureEquals(goods.getUserId(), userId, "商品不属于当前商户");

        ensureFalse(Boolean.TRUE.equals(goods.getDelete()), "商品已被删除，无法上架");

        Goods update = new Goods();
        update.setId(id);
        update.setLaunch(launch);
        merchantGoodsMapper.update(update);
        return Result.success("修改成功");
    }

    @Override
    public Result<String> deleteGoods(Integer id, Integer userId) {
        Goods exist = new Goods();
        exist.setId(id);
        Goods goods = merchantGoodsMapper.selectById(exist);
        ensureNotNull(goods, "商品不存在");
        ensureEquals(goods.getUserId(), userId, "商品不属于当前商户");

        Goods update = new Goods();
        update.setId(id);
        update.setDelete(true);
        update.setLaunch(false);
        merchantGoodsMapper.update(update);
        return Result.success("删除成功");
    }

    @Override
    public Result<MerchantGoods> getGoods(Integer id, Integer userId) {
        Goods exist = new Goods();
        exist.setId(id);
        Goods goods = merchantGoodsMapper.selectById(exist);
        ensureNotNull(goods, "商品不存在");

        MerchantGoods result = new MerchantGoods(
            goods.getId(),
            goods.getUserId(),
            goods.getGoodsName(),
            goods.getDescribe(),
            goods.getGoodsPrice(),
            goods.getGoodsStock(),
            goods.getImagePath(),
            goods.getLaunch() != null && goods.getLaunch() ? 1 : 0,
            goods.getPreId(),
            goods.getCreateTime(),
            goods.getDelete() != null && goods.getDelete() ? 1 : 0
        );
        return Result.success(result);
    }

    @Override
    public Result<Integer> getGoodsCount(Integer userId) {
        Goods query = new Goods();
        query.setUserId(userId);
        query.setDelete(false);
        List<Goods> list = merchantGoodsMapper.selectList(query);
        return Result.success(list.size());
    }

    private int resetMonthlyCountIfNeeded(User user) {
        int count = user.getUploadGoods() != null ? user.getUploadGoods() : 0;
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