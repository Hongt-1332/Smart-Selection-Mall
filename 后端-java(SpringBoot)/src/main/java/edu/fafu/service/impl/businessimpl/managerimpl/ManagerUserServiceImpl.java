package edu.fafu.service.impl.businessimpl.managerimpl;

import cn.dev33.satoken.stp.StpUtil;
import edu.fafu.database.entity.Goods;
import edu.fafu.database.entity.Result;
import edu.fafu.database.entity.User;
import edu.fafu.database.mapper.businessmapper.managermapper.ManagerUserMapper;
import edu.fafu.database.mapper.businessmapper.merchantmapper.MerchantGoodsMapper;
import edu.fafu.exception.BusinessExceptionInterface;
import edu.fafu.service.service.businessservice.managerservice.ManagerUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class ManagerUserServiceImpl implements ManagerUserService, BusinessExceptionInterface {

    @Autowired
    private ManagerUserMapper managerUserMapper;
    @Autowired
    private MerchantGoodsMapper merchantGoodsMapper;
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> updateUser(User user) {
        if (Boolean.TRUE.equals(user.getDelete())) {
            Goods query = new Goods();
            query.setUserId(user.getId());
            query.setDelete(false);
            query.setLaunch(true);
            List<Goods> onlineGoods = merchantGoodsMapper.selectList(query);
            if (!onlineGoods.isEmpty()) {
                List<Goods> updateList = new ArrayList<>(onlineGoods.size());
                for (Goods g : onlineGoods) {
                    Goods u = new Goods();
                    u.setId(g.getId());
                    u.setLaunch(false);
                    updateList.add(u);
                }
                merchantGoodsMapper.updateList(updateList);
            }

            StpUtil.kickout(user.getId());
        }

        int result = managerUserMapper.update(user);
        ensureTrue(result > 0, "修改失败");
        return Result.success("修改成功");
    }
}