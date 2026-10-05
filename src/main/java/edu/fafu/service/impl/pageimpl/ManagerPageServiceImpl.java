package edu.fafu.service.impl.pageimpl;

import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import edu.fafu.database.entity.Result;
import edu.fafu.database.dto.request.page.*;
import edu.fafu.database.dto.response.manager.*;
import edu.fafu.database.mapper.pagemapper.ManagerPageMapper;
import edu.fafu.service.service.pageservice.ManagerPageService;
import edu.fafu.tool.page.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ManagerPageServiceImpl implements ManagerPageService {

    @Autowired
    private ManagerPageMapper managerPageMapper;

    @Override
    public Result<PageResult<ManagerUser>> pageManagerUser(PageManagerUserRequest request) {
        Page<ManagerUser> page = new Page<>(request.getPage(), request.getSize());
        page.addOrder(Boolean.TRUE.equals(request.getDesc()) ? OrderItem.desc("u.id") : OrderItem.asc("u.id"));
        ManagerUser query = new ManagerUser();
        query.setId(request.getId());
        query.setUserName(request.getUserName());
        query.setAccount(request.getAccount());
        query.setPhone(request.getPhone());
        query.setEmail(request.getEmail());
        query.setDescribe(request.getDescribe());
        query.setAvatarPath(request.getImagePath());
        query.setLevel(request.getLevel());
        query.setCreateTimeStart(request.getCreateTimeStart());
        query.setCreateTimeEnd(request.getCreateTimeEnd());
        query.setDelete(request.getDelete());
        return Result.success(new PageResult<>(managerPageMapper.selectUserList(page, query)));
    }

    @Override
    public Result<PageResult<ManagerAddress>> pageManagerAddress(PageManagerAddressRequest request) {
        Page<ManagerAddress> page = new Page<>(request.getPage(), request.getSize());
        page.addOrder(Boolean.TRUE.equals(request.getDesc()) ? OrderItem.desc("a.id") : OrderItem.asc("a.id"));
        ManagerAddress query = new ManagerAddress();
        query.setId(request.getId());
        query.setUserName(request.getUserName());
        query.setCreateTimeStart(request.getCreateTimeStart());
        query.setCreateTimeEnd(request.getCreateTimeEnd());
        query.setDelete(request.getDelete());
        return Result.success(new PageResult<>(managerPageMapper.selectAddressList(page, query)));
    }

    @Override
    public Result<PageResult<ManagerCart>> pageManagerCart(PageManagerCartRequest request) {
        Page<ManagerCart> page = new Page<>(request.getPage(), request.getSize());
        page.addOrder(Boolean.TRUE.equals(request.getDesc()) ? OrderItem.desc("c.id") : OrderItem.asc("c.id"));
        ManagerCart query = new ManagerCart();
        query.setUserName(request.getUserName());
        query.setGoodsName(request.getGoodsName());
        query.setGoodsStock(request.getGoodsStock());
        query.setQuantity(request.getQuantity());
        query.setLaunch(request.getLaunch());
        query.setCreateTimeStart(request.getCreateTimeStart());
        query.setCreateTimeEnd(request.getCreateTimeEnd());
        return Result.success(new PageResult<>(managerPageMapper.selectCartList(page, query)));
    }

    @Override
    public Result<PageResult<ManagerGoods>> pageManagerGoods(PageManagerGoodsRequest request) {
        Page<ManagerGoods> page = new Page<>(request.getPage(), request.getSize());
        page.addOrder(Boolean.TRUE.equals(request.getDesc()) ? OrderItem.desc("g.id") : OrderItem.asc("g.id"));
        ManagerGoods query = new ManagerGoods();
        query.setUserName(request.getMerchantName());
        query.setGoodsName(request.getGoodsName());
        query.setAddress(request.getAddress());
        query.setPriceStart(request.getPriceStart());
        query.setPriceEnd(request.getPriceEnd());
        query.setDescribe(request.getDescribe());
        query.setLaunch(request.getLaunch());
        query.setCreateTimeStart(request.getCreateTimeStart());
        query.setCreateTimeEnd(request.getCreateTimeEnd());
        query.setDelete(request.getDelete());
        return Result.success(new PageResult<>(managerPageMapper.selectGoodsList(page, query)));
    }

    @Override
    public Result<PageResult<ManagerTrade>> pageManagerTrade(PageManagerTradeRequest request) {
        Page<ManagerTrade> page = new Page<>(request.getPage(), request.getSize());
        page.addOrder(Boolean.TRUE.equals(request.getDesc()) ? OrderItem.desc("t.id") : OrderItem.asc("t.id"));
        ManagerTrade query = new ManagerTrade();
        query.setUserName(request.getUserName());
        query.setMerchantName(request.getMerchantName());
        query.setGoodsName(request.getGoodsName());
        query.setOriginAddress(request.getOriginAddress());
        query.setCurrentAddress(request.getCurrentAddress());
        query.setTargetAddress(request.getTargetAddress());
        query.setCreateTimeStart(request.getCreateTimeStart());
        query.setCreateTimeEnd(request.getCreateTimeEnd());
        query.setPayTimeStart(request.getPayTimeStart());
        query.setPayTimeEnd(request.getPayTimeEnd());
        query.setCancelTimeStart(request.getCancelTimeStart());
        query.setCancelTimeEnd(request.getCancelTimeEnd());
        query.setFinishTimeStart(request.getFinishTimeStart());
        query.setFinishTimeEnd(request.getFinishTimeEnd());
        query.setDelete(request.getDelete());
        return Result.success(new PageResult<>(managerPageMapper.selectTradeList(page, query)));
    }

    @Override
    public Result<PageResult<ManagerVipConfig>> pageManagerVipConfig(PageManagerVipConfigRequest request) {
        Page<ManagerVipConfig> page = new Page<>(request.getPage(), request.getSize());
        ManagerVipConfig query = new ManagerVipConfig();
        query.setLevel(request.getLevel());
        query.setMaxAddressQuantity(request.getMaxAddressQuantity());
        query.setMonthlyUpdateGoods(request.getMonthlyUpdateGoods());
        query.setMonthlyUpdateAvatar(request.getMonthlyUpdateAvatar());
        query.setMonthlyUpdateBackground(request.getMonthlyUpdateBackground());
        query.setMaxCartQuantity(request.getMaxCartQuantity());
        query.setMaxGoodsQuantity(request.getMaxGoodsQuantity());
        query.setMaxAiQuantity(request.getMaxAiQuantity());
        query.setPrice(request.getPrice());
        return Result.success(new PageResult<>(managerPageMapper.selectVipConfigList(page, query)));
    }

    @Override
    public Result<PageResult<ManagerVipTrade>> pageManagerVipTrade(PageManagerVipTradeRequest request) {
        Page<ManagerVipTrade> page = new Page<>(request.getPage(), request.getSize());
        page.addOrder(Boolean.TRUE.equals(request.getDesc()) ? OrderItem.desc("vt.id") : OrderItem.asc("vt.id"));
        ManagerVipTrade query = new ManagerVipTrade();
        query.setId(request.getId());
        query.setUserName(request.getUserName());
        query.setLevel(request.getLevel());
        query.setMoneyStart(request.getMoneyStart());
        query.setMoneyEnd(request.getMoneyEnd());
        query.setCreateTimeStart(request.getCreateTimeStart());
        query.setCreateTimeEnd(request.getCreateTimeEnd());
        query.setDelete(request.getDelete());
        return Result.success(new PageResult<>(managerPageMapper.selectVipTradeList(page, query)));
    }

    @Override
    public Result<PageResult<ManagerAi>> pageManagerAi(PageManagerAiRequest request) {
        Page<ManagerAi> page = new Page<>(request.getPage(), request.getSize());
        page.addOrder(Boolean.TRUE.equals(request.getDesc()) ? OrderItem.desc("a.id") : OrderItem.asc("a.id"));
        ManagerAi query = new ManagerAi();
        query.setId(request.getId());
        query.setUserName(request.getUserName());
        query.setCategory(request.getCategory());
        query.setKind(request.getKind());
        query.setName(request.getName());
        query.setPriceStart(request.getPriceStart());
        query.setPriceEnd(request.getPriceEnd());
        query.setSimpleDescription(request.getSimpleDescription());
        query.setFeatures(request.getFeatures());
        query.setCreateTimeStart(request.getCreateTimeStart());
        query.setCreateTimeEnd(request.getCreateTimeEnd());
        query.setDelete(request.getDelete());
        return Result.success(new PageResult<>(managerPageMapper.selectAiList(page, query)));
    }

    @Override
    public Result<PageResult<ManagerGoods>> pageManagerHistoryGoods(PageManagerHistoryGoodsRequest request) {
        Page<ManagerGoods> page = new Page<>(request.getPage(), request.getSize());
        page.addOrder(Boolean.TRUE.equals(request.getDesc()) ? OrderItem.desc("gc.id") : OrderItem.asc("gc.id"));
        return Result.success(new PageResult<>(managerPageMapper.selectGoodsChain(page, request.getGoodsId())));
    }
}