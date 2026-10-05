package edu.fafu.service.impl.toolimpl;

import edu.fafu.config.SystemConfig;
import edu.fafu.database.entity.Result;
import edu.fafu.exception.BusinessExceptionInterface;
import edu.fafu.service.service.toolservice.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Service
public class FileServiceImpl implements FileService, BusinessExceptionInterface {

    @Autowired
    private SystemConfig systemConfig;

    @Override
    public Result<String> uploadFile(MultipartFile file, Integer userId) {
        ensureTrue(file != null && !file.isEmpty(), "文件不能为空");
        ensureTrue(file.getSize() <= systemConfig.getFileMaxUploadSize(), "文件大小不能超过" + systemConfig.getFileMaxUploadSizeMb() + "MB");

        String originalName = file.getOriginalFilename();
        String ext = "";
        if (originalName != null && originalName.contains(".")) {
            ext = originalName.substring(originalName.lastIndexOf(".")).toLowerCase();
        }

        String fileName = UUID.randomUUID().toString().replace("-", "") + "_" + System.currentTimeMillis() + ext;
        String absolutePath = systemConfig.getFileBaseDir() + "/" + userId + "/" + fileName;

        File dirFile = new File(absolutePath).getParentFile();
        if (!dirFile.exists()) dirFile.mkdirs();

        try {
            file.transferTo(new File(absolutePath));
        } catch (IOException e) {
            fail("文件保存失败");
        }

        String relativePath = "/file/" + userId + "/" + fileName;
        return Result.success(relativePath);
    }

    @Override
    public Result<FileData> getFile(String filePath) {
        ensureNotBlank(filePath, "路径不能为空");

        Path baseDir = Path.of(systemConfig.getFileBaseDir()).toAbsolutePath().normalize();

        if (filePath.startsWith("/file/")) {
            filePath = systemConfig.getFileBaseDir() + filePath.substring("/file".length());
        }

        Path resolved = Path.of(filePath).toAbsolutePath().normalize();
        ensureTrue(resolved.startsWith(baseDir), "非法路径");

        File file = resolved.toFile();
        ensureTrue(file.exists() && file.isFile(), "文件不存在");

        try {
            byte[] data = Files.readAllBytes(file.toPath());
            String contentType = Files.probeContentType(file.toPath());
            if (contentType == null) contentType = "application/octet-stream";
            return Result.success(new FileData(data, contentType, data.length));
        } catch (IOException e) {
            fail("文件读取失败");
            return null;
        }
    }
}