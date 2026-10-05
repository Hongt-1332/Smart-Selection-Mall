package edu.fafu.service.service.businessservice.userservice;

import edu.fafu.database.entity.Result;
import edu.fafu.database.dto.request.user.LoginRequest;
import edu.fafu.database.dto.request.user.RegisterRequest;
import edu.fafu.database.dto.request.user.WechatLoginRequest;
import edu.fafu.database.dto.request.user.UpdateRequest;
import edu.fafu.database.dto.response.user.UserInfoVO;
import edu.fafu.database.dto.response.user.WechatLoginResponse;
import org.springframework.web.multipart.MultipartFile;

public interface UserUserService {

    Result<String> register(RegisterRequest request);

    Result<java.util.Map<String, String>> login(LoginRequest request);

    Result<WechatLoginResponse> wechatLogin(WechatLoginRequest request);

    Result<String> update(UpdateRequest request, Integer userId);

    Result<String> setDefaultAddress(Integer addressId, Integer userId);

    Result<String> logout(Integer userId);

    Result<String> updateUserImage(MultipartFile file, Integer userId);

    Result<String> updateDescribe(String describe, Integer userId);

    Result<UserInfoVO> getUserInfo(Integer userId);
}