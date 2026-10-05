package edu.fafu.tool.crypto;

import edu.fafu.config.SystemConfig;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * PathCryptoUtil 图片路径加解密测试
 *
 * 图片路径格式: /image/{userId}/{subDir}/{timestamp}_{random}.webp
 * 示例: /image/1/goods/1719123456_1234.webp
 */
public class PathCryptoUtilTest implements PathCryptoUtil {

    static SystemConfig config;

    @BeforeAll
    static void setup() {
        config = new SystemConfig();
        config.setPathCryptoSeed("WebImagePathSeed2026!@#fafu");
    }

    @Test
    @DisplayName("相对路径加解密一致性")
    void testRelativePathEncryptDecrypt() {
        String[] paths = {
                "/image/1/goods/1719123456_1234.webp",
                "/image/42/goods/1719123456_5678.webp",
                "/image/100/goods/1719123456_9012.png",
                "/image/7/avatar/1719123456_3456.jpeg",
                "/image/99/goods/1719123456_7890.gif"
        };
        for (String original : paths) {
            String encrypted = PathCryptoUtil.encrypt(config, original);
            String decrypted = PathCryptoUtil.decrypt(config, encrypted);
            assertEquals(original, decrypted, "加解密不一致: " + original);
            assertNotEquals(original, encrypted, "相对路径应该被加密: " + original);
        }
    }

    @Test
    @DisplayName("本地路径不加密")
    void testLocalPathNotEncrypted() {
        String localPath = "C:/image/1/goods/test.webp";
        assertEquals(localPath, PathCryptoUtil.encrypt(config, localPath));
        assertEquals(localPath, PathCryptoUtil.decrypt(config, localPath));
    }

    @Test
    @DisplayName("OSS URL不加密 - https")
    void testHttpsUrlNotEncrypted() {
        String ossUrl = "https://kaixinregion.oss-cn-wuhan-lr.aliyuncs.com/image/goods/1/product_1.png";
        assertEquals(ossUrl, PathCryptoUtil.encrypt(config, ossUrl), "https OSS URL不应被加密");
        assertEquals(ossUrl, PathCryptoUtil.decrypt(config, ossUrl), "https OSS URL不应被解密");
    }

    @Test
    @DisplayName("OSS URL不加密 - http")
    void testHttpUrlNotEncrypted() {
        String httpUrl = "http://kaixinregion.oss-cn-wuhan-lr.aliyuncs.com/image/avatar/2/avatar.png";
        assertEquals(httpUrl, PathCryptoUtil.encrypt(config, httpUrl), "http URL不应被加密");
        assertEquals(httpUrl, PathCryptoUtil.decrypt(config, httpUrl), "http URL不应被解密");
    }

    @Test
    @DisplayName("OSS URL各种路径格式不加密")
    void testOssUrlVariants() {
        String[] ossUrls = {
                "https://kaixinregion.oss-cn-wuhan-lr.aliyuncs.com/image/goods/1/product_1.png",
                "https://kaixinregion.oss-cn-wuhan-lr.aliyuncs.com/image/avatar/2/avatar.png",
                "https://kaixinregion.oss-cn-wuhan-lr.aliyuncs.com/image/background/1/bg.png",
                "https://kaixinregion.oss-cn-wuhan-lr.aliyuncs.com/goods/1/1719123456_1234.webp",
                "http://example.com/image/test.jpg"
        };
        for (String url : ossUrls) {
            assertEquals(url, PathCryptoUtil.encrypt(config, url), "OSS URL不应被加密: " + url);
            assertEquals(url, PathCryptoUtil.decrypt(config, url), "OSS URL不应被解密: " + url);
        }
    }

    @Test
    @DisplayName("null和空值处理")
    void testNullAndEmpty() {
        assertNull(PathCryptoUtil.encrypt(config, null));
        assertNull(PathCryptoUtil.decrypt(config, null));
        assertEquals("", PathCryptoUtil.encrypt(config, ""));
        assertEquals("", PathCryptoUtil.decrypt(config, ""));
    }

    @Test
    @DisplayName("篡改密文解密返回null")
    void testTamperedCipherReturnsNull() {
        String original = "/image/1/goods/1719123456_1234.webp";
        String encrypted = PathCryptoUtil.encrypt(config, original);
        String tampered = encrypted.substring(0, encrypted.length() - 1) + "0";
        assertNull(PathCryptoUtil.decrypt(config, tampered));
    }
}