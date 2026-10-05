package edu.fafu.service.service.businessservice.managerservice;

import edu.fafu.database.dto.request.manager.UpdateManagerBackgroundRequest;
import edu.fafu.database.entity.Result;

public interface ManagerBackgroundService {

    Result<String> updateBackground(UpdateManagerBackgroundRequest request);
}