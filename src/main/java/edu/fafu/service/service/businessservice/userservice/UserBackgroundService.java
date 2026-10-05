package edu.fafu.service.service.businessservice.userservice;

import edu.fafu.database.dto.request.user.AddBackgroundRequest;
import edu.fafu.database.dto.request.user.PaymentActionRequest;
import edu.fafu.database.dto.request.user.UpdateBackgroundRequest;
import edu.fafu.database.entity.Result;

public interface UserBackgroundService {

    Result<String> addBackground(AddBackgroundRequest request, Integer userId);

    Result<String> updateBackground(UpdateBackgroundRequest request, Integer userId);

    Result<String> pay(PaymentActionRequest request, Integer userId);
}