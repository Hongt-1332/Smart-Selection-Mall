package edu.fafu.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.system")
public class SystemConfig {

    private int tokenExpireDays = 7;
    private String tokenSecret;
    private int loginMaxAttempts = 5;
    private long loginWindowSec = 60;
    private String fileBaseDir = "C:/file";
    private long fileMaxUploadSizeMb = 10;
    private String imageAllowedExt = ".jpg.jpeg.png.gif.webp";
    private long imageMaxSizeMb = 5;
    private int imageBatchMax = 50;
    private int tradeCancelTimeoutMinutes = 30;
    private int tradeMerchantCancelTimeoutHours = 1;
    private BigDecimal goodsMinPrice = new BigDecimal("0.01");
    private int captchaWidth = 120;
    private int captchaHeight = 40;
    private int captchaCodeLength = 4;
    private int userMaxAddresses = 5;
    private int backgroundSequenceCount = 4;
    private int userMaxBackgrounds = 10;
    private String pathCryptoSeed;
    private String imageBaseDir = "C:/image";
    private int userUpdateCountExpireDays = 32;
    private String fileDownloadPrefix = "C:";
    private String imageDownloadPrefix = "C:";

    // AI 配置
    private int aiChatMaxMessages = 20;
    private int aiMemoryTtlMinutes = 10;
    private String aiBaseUrl = "http://localhost:11434";
    private String aiDefaultModel = "qwen2.5:7b";
    private String aiEmbeddingModel = "nomic-embed-text";
    private int aiEmbeddingBatchSize = 100;
    private int aiRagMaxResults = 3;
    private double aiRagMinScore = 0.6;

    // OSS 配置
    private String ossEndpoint;
    private String ossRegion;
    private String ossBucket;
    private String ossAccessKeyId;
    private String ossAccessKeySecret;

    // 微信小程序配置
    private String wechatAppId;           // 小程序 AppID
    private String wechatAppSecret;       // 小程序 AppSecret

    private long tokenExpireMs;
    private long fileMaxUploadSize;
    private long imageMaxSize;

    @PostConstruct
    public void init() {
        tokenExpireMs = tokenExpireDays * 24L * 3600 * 1000;
        fileMaxUploadSize = fileMaxUploadSizeMb * 1024 * 1024;
        imageMaxSize = imageMaxSizeMb * 1024 * 1024;

        // 安全启动校验：这些安全令牌/种子必须通过环境变量显式注入，
        // 禁止使用硬编码弱默认值，缺失时立即失败以避免在弱密钥下运行
        requireSecret(tokenSecret, "TOKEN_SECRET");
        requireSecret(pathCryptoSeed, "PATH_CRYPTO_SEED");
    }

    private void requireSecret(String value, String envName) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                    "安全配置缺失：请通过环境变量 " + envName + " 提供非空的强随机密钥后再启动服务。");
        }
        if (value.length() < 16) {
            throw new IllegalStateException(
                    "安全配置过弱：环境变量 " + envName + " 长度应不小于 16 位，请使用强随机密钥。");
        }
    }
}