package edu.fafu.tool.oss;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PutObjectResult;
import edu.fafu.config.SystemConfig;
import edu.fafu.exception.BusinessException;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

@Component
public class OssUtil {

    @Autowired
    private SystemConfig systemConfig;

    private OSS client;

    public void init() {
        client = new OSSClientBuilder().build(
                systemConfig.getOssEndpoint(), systemConfig.getOssAccessKeyId(), systemConfig.getOssAccessKeySecret());
    }

    @PreDestroy
    public void destroy() {
        if (client != null) {
            client.shutdown();
        }
    }

    private OSS getClient() {
        if (client == null) {
            synchronized (this) {
                if (client == null) {
                    init();
                }
            }
        }
        return client;
    }

    public String upload(String key, MultipartFile file) {
        try {
            ObjectMetadata meta = new ObjectMetadata();
            meta.setContentLength(file.getSize());
            String contentType = file.getContentType();
            if (contentType != null) {
                meta.setContentType(contentType);
            }
            try (InputStream is = file.getInputStream()) {
                getClient().putObject(systemConfig.getOssBucket(), key, is, meta);
            }
            return getUrl(key);
        } catch (Exception e) {
            throw new BusinessException("OSS上传失败: " + e.getMessage());
        }
    }

    public String upload(String key, InputStream inputStream, long contentLength, String contentType) {
        try {
            ObjectMetadata meta = new ObjectMetadata();
            meta.setContentLength(contentLength);
            if (contentType != null) {
                meta.setContentType(contentType);
            }
            getClient().putObject(systemConfig.getOssBucket(), key, inputStream, meta);
            return getUrl(key);
        } catch (Exception e) {
            throw new BusinessException("OSS上传失败: " + e.getMessage());
        }
    }

    public void delete(String key) {
        try {
            getClient().deleteObject(systemConfig.getOssBucket(), key);
        } catch (Exception e) {
            throw new BusinessException("OSS删除失败: " + e.getMessage());
        }
    }

    public boolean exists(String key) {
        try {
            return getClient().doesObjectExist(systemConfig.getOssBucket(), key);
        } catch (Exception e) {
            throw new BusinessException("OSS检查失败: " + e.getMessage());
        }
    }

    public String getUrl(String key) {
        return "https://" + systemConfig.getOssBucket() + "." + systemConfig.getOssEndpoint()
                .replace("https://", "").replace("http://", "") + "/" + key;
    }
}