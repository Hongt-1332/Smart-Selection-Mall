package edu.fafu.tool.image;

import edu.fafu.config.SystemConfig;
import edu.fafu.exception.BusinessException;
import edu.fafu.tool.oss.OssUtil;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

public interface ImageUtil {

    Map<String, String> EXT_TO_CONTENT_TYPE = Map.of(
            ".png", "image/png",
            ".jpg", "image/jpeg",
            ".jpeg", "image/jpeg",
            ".gif", "image/gif",
            ".webp", "image/webp",
            ".bmp", "image/bmp"
    );

    static void validateImage(SystemConfig config, MultipartFile file) {
        if (file == null || file.isEmpty()) throw new BusinessException("图片不能为空");

        String originalName = file.getOriginalFilename();
        String ext = "";
        if (originalName != null && originalName.contains(".")) {
            ext = originalName.substring(originalName.lastIndexOf(".")).toLowerCase();
        }
        if (!config.getImageAllowedExt().contains(ext)) {
            throw new BusinessException("不支持的图片格式");
        }
        if (file.getSize() > config.getImageMaxSize()) {
            throw new BusinessException("图片大小不能超过" + (config.getImageMaxSize() / 1024 / 1024) + "MB");
        }
    }

    static String getFileExt(MultipartFile file) {
        String originalName = file.getOriginalFilename();
        if (originalName != null && originalName.contains(".")) {
            return originalName.substring(originalName.lastIndexOf(".")).toLowerCase();
        }
        return ".png";
    }

    static String saveImageToOss(SystemConfig config, OssUtil ossUtil, MultipartFile file, String ossKey) {
        validateImage(config, file);

        String ext = getFileExt(file);
        String actualOssKey = ossKey.replaceAll("\\.webp$", "") + ext;
        String contentType = EXT_TO_CONTENT_TYPE.getOrDefault(ext, "image/jpeg");

        try {
            byte[] bytes = file.getBytes();
            ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
            return ossUtil.upload(actualOssKey, bais, bytes.length, contentType);
        } catch (IOException e) {
            throw new BusinessException("图片保存到OSS失败");
        }
    }

    static void deleteOssFile(OssUtil ossUtil, String ossKey) {
        ossUtil.delete(ossKey);
    }

    static String saveAvatarToOss(SystemConfig config, OssUtil ossUtil, MultipartFile file, Integer userId) {
        String ext = getFileExt(file);
        String fileName = "avatar_" + userId + "_" + System.currentTimeMillis() + ext;
        String ossKey = "avatar/" + fileName;
        return saveImageToOss(config, ossUtil, file, ossKey);
    }

    static String guessContentType(Path path) {
        String name = path.getFileName().toString().toLowerCase();
        if (name.endsWith(".webp")) return "image/webp";
        if (name.endsWith(".png")) return "image/png";
        if (name.endsWith(".gif")) return "image/gif";
        return "image/jpeg";
    }
}