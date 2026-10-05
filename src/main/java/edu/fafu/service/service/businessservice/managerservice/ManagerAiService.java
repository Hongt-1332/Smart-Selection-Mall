package edu.fafu.service.service.businessservice.managerservice;

import edu.fafu.database.entity.Ai;
import edu.fafu.database.entity.Result;

public interface ManagerAiService {
    Result<String> updateAi(Ai ai);
}