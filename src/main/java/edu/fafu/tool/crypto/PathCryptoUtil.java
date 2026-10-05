package edu.fafu.tool.crypto;

import cn.hutool.crypto.Mode;
import cn.hutool.crypto.Padding;
import cn.hutool.crypto.symmetric.AES;
import edu.fafu.config.SystemConfig;
import edu.fafu.exception.BusinessException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

public interface PathCryptoUtil {

    DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    Pattern LOCAL_PATH_PATTERN = Pattern.compile("^[A-Za-z]:[/\\\\].*");

    static AES createCipher(SystemConfig config, LocalDate date) {
        try {
            String raw = config.getPathCryptoSeed() + date.format(DATE_FMT);
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(raw.getBytes(StandardCharsets.UTF_8));
            byte[] key = new byte[16];
            byte[] iv = new byte[16];
            System.arraycopy(hash, 0, key, 0, 16);
            System.arraycopy(hash, 16, iv, 0, 16);
            return new AES(Mode.CBC, Padding.PKCS5Padding, key, iv);
        } catch (Exception e) {
            throw new BusinessException("路径加密初始化失败");
        }
    }

    static boolean isLocalPath(String path) {
        return path != null && LOCAL_PATH_PATTERN.matcher(path).matches();
    }

    static boolean isHttpUrl(String path) {
        return path != null && (path.startsWith("http://") || path.startsWith("https://"));
    }

    static String encrypt(SystemConfig config, String plainPath) {
        if (plainPath == null || plainPath.isBlank()) return plainPath;
        if (isLocalPath(plainPath) || isHttpUrl(plainPath)) return plainPath;
        return createCipher(config, LocalDate.now()).encryptHex(plainPath);
    }

    static String decrypt(SystemConfig config, String cipherPath) {
        if (cipherPath == null || cipherPath.isBlank()) return cipherPath;
        if (isLocalPath(cipherPath) || isHttpUrl(cipherPath)) return cipherPath;
        LocalDate today = LocalDate.now();
        try {
            return createCipher(config, today).decryptStr(cipherPath);
        } catch (Exception e) {
            return null;
        }
    }
}