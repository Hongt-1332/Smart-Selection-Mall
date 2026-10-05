package edu.fafu.service.impl.businessimpl.userimpl;

import edu.fafu.config.VipConfigCache;
import edu.fafu.database.entity.*;
import edu.fafu.database.dto.request.user.CartActionRequest;
import edu.fafu.database.mapper.businessmapper.usermapper.*;
import edu.fafu.exception.BusinessExceptionInterface;
import edu.fafu.service.service.businessservice.userservice.UserCartService;
import edu.fafu.tool.cache.QueryCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserCartServiceImpl implements UserCartService, BusinessExceptionInterface {

    private static final String CACHE_KEY_CART_COUNT = "user:cart:count:";

    @Autowired
    private UserCartMapper userCartMapper;
    @Autowired
    private VipConfigCache vipConfigCache;
    @Autowired
    private UserGoodsMapper userGoodsMapper;
    @Autowired
    private UserTradeMapper userTradeMapper;
    @Autowired
    private UserUserMapper userUserMapper;
    @Autowired
    private UserAddressMapper userAddressMapper;
    @Autowired
    private QueryCache queryCache;

    @Override
    public Result<String> handleCart(CartActionRequest request, Integer userId) {
        Goods goodsQuery = new Goods();
        goodsQuery.setId(request.getGoodId());
        Goods goods = userGoodsMapper.selectById(goodsQuery);
        ensureNotNull(goods, "商品不存在");

        ensureNotEquals(goods.getUserId(), userId, "不能购买自己的商品");

        Cart cartQuery = new Cart();
        cartQuery.setUserId(userId);
        cartQuery.setGoodId(request.getGoodId());
        Cart existCart = userCartMapper.selectById(cartQuery);

        if (request.getNum() <= 0) {
            ensureNotNull(existCart, "购物车中无该商品");
            ensureEquals(existCart.getUserId(), userId, "购物车项不属于当前用户");
            userCartMapper.delete(existCart);
            return Result.success("删除成功");
        }

        ensureTrue(Boolean.TRUE.equals(goods.getLaunch()), "商品已下架");

        if (existCart != null) {
            existCart.setQuantity(request.getNum());
            userCartMapper.update(existCart);
            return Result.success("修改成功");
        }

        User userQuery = new User();
        userQuery.setId(userId);
        User user = userUserMapper.selectById(userQuery);
        ensureNotNull(user, "用户不存在");

        int maxCart = vipConfigCache.getMaxCartQuantity(user.getLevel() != null ? user.getLevel() : 0);
        Cart countQuery = new Cart();
        countQuery.setUserId(userId);
        List<Cart> existCarts = userCartMapper.selectList(countQuery);
        ensureTrue(existCarts.size() < maxCart, "购物车数量已达上限(" + maxCart + "个)");

        Cart cart = new Cart();
        cart.setUserId(userId);
        cart.setGoodId(goods.getId());
        cart.setQuantity(request.getNum());
        cart.setCreateTime(LocalDateTime.now());
        userCartMapper.insert(cart);

        return Result.success("添加成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> buyFromCart(List<Integer> cartIds, Integer userId) {
        ensureNotEmpty(cartIds, "购物车为空");

        List<Cart> oldCarts = userCartMapper.selectByIds(cartIds);
        ensureTrue(oldCarts.size() == cartIds.size(), "部分购物车项不存在");

        for (Cart old : oldCarts) {
            ensureEquals(old.getUserId(), userId, "购物车项不属于当前用户");
        }

        List<Integer> goodsIds = oldCarts.stream().map(Cart::getGoodId).distinct().collect(Collectors.toList());
        List<Goods> goodsList = userGoodsMapper.selectByIds(goodsIds);
        Map<Integer, Goods> goodsMap = goodsList.stream().collect(Collectors.toMap(Goods::getId, g -> g));

        for (Cart old : oldCarts) {
            Goods goods = goodsMap.get(old.getGoodId());
            ensureNotNull(goods, "商品不存在");
            ensureNotEquals(goods.getUserId(), userId, "不能购买自己的商品: " + goods.getGoodsName());
            ensureTrue(goods.getGoodsStock() != null && goods.getGoodsStock() >= old.getQuantity(), "库存不足: " + goods.getGoodsName());
            ensureTrue(Boolean.TRUE.equals(goods.getLaunch()), "商品已下架: " + goods.getGoodsName());
        }

        User userParam = new User();
        userParam.setId(userId);
        User user = userUserMapper.selectById(userParam);
        ensureTrue(user != null && user.getDefaultAddressId() != null, "请先设置默认收货地址");

        Address addressParam = new Address();
        addressParam.setId(user.getDefaultAddressId());
        Address address = userAddressMapper.selectById(addressParam);
        ensureNotNull(address, "收货地址不存在");

        List<Integer> addressIds = goodsList.stream().map(Goods::getAddressId).distinct().collect(Collectors.toList());
        List<Address> addressList = userAddressMapper.selectByIds(addressIds);
        Map<Integer, Address> addressMap = addressList.stream().collect(Collectors.toMap(Address::getId, a -> a));

        BigDecimal totalDeduct = BigDecimal.ZERO;
        Map<Integer, BigDecimal> merchantBalanceMap = new HashMap<>();

        for (Cart old : oldCarts) {
            Goods goods = goodsMap.get(old.getGoodId());
            BigDecimal itemTotal = goods.getGoodsPrice().multiply(BigDecimal.valueOf(old.getQuantity()));
            totalDeduct = totalDeduct.add(itemTotal);
            merchantBalanceMap.merge(goods.getUserId(), itemTotal, BigDecimal::add);
        }

        User buyerQuery = new User();
        buyerQuery.setId(userId);
        User buyer = userUserMapper.selectById(buyerQuery);
        ensureSufficientBalance(buyer.getBalance(), totalDeduct, "余额不足，需要 " + totalDeduct + "，当前 " + buyer.getBalance());

        List<Trade> trades = new ArrayList<>();
        List<Goods> goodsUpdates = new ArrayList<>();

        for (Cart old : oldCarts) {
            Goods goods = goodsMap.get(old.getGoodId());
            Address goodsAddress = addressMap.get(goods.getAddressId());

            Trade trade = new Trade();
            trade.setOrderId(UUID.randomUUID().toString().replace("-", ""));
            trade.setUserId(userId);
            trade.setGoodId(old.getGoodId());
            trade.setQuantity(old.getQuantity());
            trade.setOriginAddress(goodsAddress != null ? goodsAddress.getDetail() : "");
            trade.setTargetAddress(address.getDetail());
            trade.setCurrentAddress(goodsAddress != null ? goodsAddress.getDetail() : "");
            trade.setCreateTime(LocalDateTime.now());
            trade.setDelete(false);
            trades.add(trade);

            Goods stockUpdate = new Goods();
            stockUpdate.setId(goods.getId());
            stockUpdate.setGoodsStock(goods.getGoodsStock() - old.getQuantity());
            goodsUpdates.add(stockUpdate);
        }

        buyer.setBalance(buyer.getBalance().subtract(totalDeduct));
        userUserMapper.update(buyer);

        List<Integer> merchantIds = new ArrayList<>(merchantBalanceMap.keySet());
        List<User> merchants = userUserMapper.selectByIds(merchantIds);
        for (User merchant : merchants) {
            BigDecimal addAmount = merchantBalanceMap.get(merchant.getId());
            merchant.setBalance(merchant.getBalance().add(addAmount));
        }
        userUserMapper.updateList(merchants);

        userTradeMapper.insertList(trades);
        userGoodsMapper.updateList(goodsUpdates);
        userCartMapper.deleteList(oldCarts);

        return Result.success("购买成功");
    }

    @Override
    public Result<Integer> getCartCount(Integer userId) {
        return Result.success(queryCache.getOrLoadInt(
                CACHE_KEY_CART_COUNT + userId,
                () -> userCartMapper.countByUserId(userId),
                1
        ));
    }
}