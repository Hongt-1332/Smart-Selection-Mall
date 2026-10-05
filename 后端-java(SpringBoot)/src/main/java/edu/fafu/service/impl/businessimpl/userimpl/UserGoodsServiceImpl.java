package edu.fafu.service.impl.businessimpl.userimpl;

import edu.fafu.config.VipConfigCache;
import edu.fafu.database.entity.*;
import edu.fafu.database.dto.request.user.BuyGoodsRequest;
import edu.fafu.database.mapper.businessmapper.usermapper.*;
import edu.fafu.exception.BusinessExceptionInterface;
import edu.fafu.service.service.businessservice.userservice.UserGoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class UserGoodsServiceImpl implements UserGoodsService, BusinessExceptionInterface {

    @Autowired
    private UserGoodsMapper userGoodsMapper;
    @Autowired
    private UserUserMapper userUserMapper;
    @Autowired
    private UserAddressMapper userAddressMapper;
    @Autowired
    private UserTradeMapper userTradeMapper;
    @Autowired
    private UserCartMapper userCartMapper;
    @Autowired
    private VipConfigCache vipConfigCache;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> buyGoods(BuyGoodsRequest request, Integer userId) {
        Goods goodsQuery = new Goods();
        goodsQuery.setId(request.getGoodsId());
        Goods goods = userGoodsMapper.selectById(goodsQuery);
        ensureNotNull(goods, "商品不存在");
        ensureTrue(goods.getGoodsStock() != null && goods.getGoodsStock() >= request.getQuantity(), "库存不足");
        ensureTrue(Boolean.TRUE.equals(goods.getLaunch()), "商品已下架");

        Address addressQuery = new Address();
        addressQuery.setId(request.getShippingAddressId());
        Address address = userAddressMapper.selectById(addressQuery);
        ensureNotNull(address, "收货地址不存在");
        ensureEquals(address.getUserId(), userId, "收货地址不属于当前用户");

        User buyerQuery = new User();
        buyerQuery.setId(userId);
        User buyer = userUserMapper.selectById(buyerQuery);
        ensureNotNull(buyer, "用户不存在");

        BigDecimal total = goods.getGoodsPrice().multiply(BigDecimal.valueOf(request.getQuantity()));
        ensureSufficientBalance(buyer.getBalance(), total, "余额不足");

        Goods goodsAddressQuery = new Goods();
        goodsAddressQuery.setId(goods.getId());
        String originAddress = "";
        if (goods.getAddressId() != null) {
            Address addrQuery = new Address();
            addrQuery.setId(goods.getAddressId());
            Address goodsAddress = userAddressMapper.selectById(addrQuery);
            if (goodsAddress != null) originAddress = goodsAddress.getDetail();
        }

        Trade trade = new Trade();
        trade.setOrderId(UUID.randomUUID().toString().replace("-", ""));
        trade.setUserId(userId);
        trade.setGoodId(goods.getId());
        trade.setQuantity(request.getQuantity());
        trade.setOriginAddress(originAddress);
        trade.setTargetAddress(address.getDetail());
        trade.setCurrentAddress(originAddress);
        trade.setCreateTime(LocalDateTime.now());
        trade.setDelete(false);
        userTradeMapper.insert(trade);

        Goods stockUpdate = new Goods();
        stockUpdate.setId(goods.getId());
        stockUpdate.setGoodsStock(goods.getGoodsStock() - request.getQuantity());
        userGoodsMapper.update(stockUpdate);

        buyer.setBalance(buyer.getBalance().subtract(total));
        userUserMapper.update(buyer);

        User merchantQuery = new User();
        merchantQuery.setId(goods.getUserId());
        User merchant = userUserMapper.selectById(merchantQuery);
        if (merchant != null) {
            merchant.setBalance(merchant.getBalance().add(total));
            userUserMapper.update(merchant);
        }

        return Result.success("购买成功");
    }
}