package edu.fafu.controller.toolcontroller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import edu.fafu.database.entity.Result;
import edu.fafu.service.service.toolservice.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@SaCheckLogin
@RestController
@RequestMapping("/file")
public class FileController {

    @Autowired
    private FileService fileService;

    @PostMapping("/upload")
    public Result<String> uploadFile(@RequestParam("file") MultipartFile file) {
        return fileService.uploadFile(file, StpUtil.getLoginIdAsInt());
    }

    @GetMapping("/get")
    public ResponseEntity<?> getFile(@RequestParam("path") String filePath) {
        Result<FileService.FileData> result = fileService.getFile(filePath);
        if (result.getCode() != 200) {
            return ResponseEntity.status(404).body(result);
        }

        FileService.FileData fileData = result.getData();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, fileData.contentType())
                .contentLength(fileData.contentLength())
                .body(new ByteArrayResource(fileData.data()));
    }
}