package edu.fafu.service.service.toolservice;

import edu.fafu.database.entity.Result;
import org.springframework.web.multipart.MultipartFile;

public interface ToolService {

    Result<String> uploadImage(MultipartFile file, Integer userId);
}