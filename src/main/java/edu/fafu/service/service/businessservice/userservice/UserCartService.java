package edu.fafu.service.service.businessservice.userservice;

import edu.fafu.database.entity.Result;
import edu.fafu.database.dto.request.user.CartActionRequest;

import java.util.List;

public interface UserCartService {

    Result<String> handleCart(CartActionRequest request, Integer userId);

    Result<String> buyFromCart(List<Integer> cartIds, Integer userId);

    Result<Integer> getCartCount(Integer userId);
}