package edu.fafu.service.impl.businessimpl.managerimpl;

import edu.fafu.database.dto.request.manager.UpdateManagerGoodsRequest;
import edu.fafu.database.entity.Goods;
import edu.fafu.database.entity.Result;
import edu.fafu.database.mapper.businessmapper.managermapper.ManagerGoodsMapper;
import edu.fafu.exception.BusinessExceptionInterface;
import edu.fafu.service.service.businessservice.managerservice.ManagerGoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ManagerGoodsServiceImpl implements ManagerGoodsService, BusinessExceptionInterface {

    @Autowired
    private ManagerGoodsMapper managerGoodsMapper;

    @Override
    public Result<String> updateGoods(UpdateManagerGoodsRequest request) {
        Goods current = managerGoodsMapper.selectById(request.getId());
        ensureNotNull(current, "商品不存在");

        Goods goods = new Goods();
        goods.setUserId(request.getUserId() != null ? request.getUserId() : current.getUserId());
        goods.setGoodsId(request.getGoodsId() != null ? request.getGoodsId() : current.getGoodsId());
        goods.setAddressId(request.getAddressId() != null ? request.getAddressId() : current.getAddressId());
        goods.setGoodsName(request.getGoodsName() != null ? request.getGoodsName() : current.getGoodsName());
        goods.setDescribe(request.getDescribe() != null ? request.getDescribe() : current.getDescribe());
        goods.setGoodsPrice(request.getGoodsPrice() != null ? request.getGoodsPrice() : current.getGoodsPrice());
        goods.setGoodsStock(request.getGoodsStock() != null ? request.getGoodsStock() : current.getGoodsStock());
        goods.setImagePath(request.getImagePath() != null ? request.getImagePath() : current.getImagePath());
        goods.setDelete(request.getDelete() != null ? request.getDelete() : current.getDelete());

        if (Boolean.TRUE.equals(goods.getDelete())) {
            goods.setLaunch(false);
        } else {
            goods.setLaunch(request.getLaunch() != null ? request.getLaunch() : current.getLaunch());
        }

        goods.setPreId(current.getId());
        goods.setId(null);

        int result = managerGoodsMapper.updateList(List.of(goods));
        ensureTrue(result > 0, "修改失败");
        return Result.success("修改成功");
    }
}