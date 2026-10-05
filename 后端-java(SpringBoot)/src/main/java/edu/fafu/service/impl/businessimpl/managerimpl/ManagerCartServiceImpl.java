package edu.fafu.service.impl.businessimpl.managerimpl;

import edu.fafu.database.entity.Cart;
import edu.fafu.database.entity.Result;
import edu.fafu.database.mapper.businessmapper.managermapper.ManagerCartMapper;
import edu.fafu.exception.BusinessExceptionInterface;
import edu.fafu.service.service.businessservice.managerservice.ManagerCartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ManagerCartServiceImpl implements ManagerCartService, BusinessExceptionInterface {

    @Autowired
    private ManagerCartMapper managerCartMapper;

    @Override
    public Result<String> deleteCart(Integer id) {
        Cart cart = new Cart();
        cart.setId(id);
        int result = managerCartMapper.delete(cart);
        ensureTrue(result > 0, "删除失败");
        return Result.success("删除成功");
    }
}