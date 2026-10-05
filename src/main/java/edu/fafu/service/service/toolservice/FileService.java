package edu.fafu.service.service.toolservice;

import edu.fafu.database.entity.Result;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {

    Result<String> uploadFile(MultipartFile file, Integer userId);

    Result<FileData> getFile(String filePath);

    record FileData(byte[] data, String contentType, long contentLength) {}
}