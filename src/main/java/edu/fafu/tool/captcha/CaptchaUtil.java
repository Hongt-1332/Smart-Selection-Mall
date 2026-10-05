package edu.fafu.tool.captcha;

import edu.fafu.config.SystemConfig;
import edu.fafu.exception.BusinessException;
import jakarta.servlet.http.HttpSession;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.QuadCurve2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Random;

public interface CaptchaUtil {

    String SESSION_KEY = "captchaCode";
    String CHAR_SOURCE = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

    static HttpSession getSession() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        return attrs.getRequest().getSession();
    }

    static VO generate(SystemConfig config) {
        String code = generateCode(config);
        getSession().setAttribute(SESSION_KEY, code);
        String imageBase64 = generateImage(config, code);
        return new VO(imageBase64);
    }

    static boolean validate(String captchaCode) {
        if (captchaCode == null) return false;
        HttpSession session = getSession();
        String storedCode = (String) session.getAttribute(SESSION_KEY);
        if (storedCode == null) return false;
        session.removeAttribute(SESSION_KEY);
        return storedCode.equalsIgnoreCase(captchaCode);
    }

    static String generateCode(SystemConfig config) {
        int codeLength = config.getCaptchaCodeLength();
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < codeLength; i++) {
            sb.append(CHAR_SOURCE.charAt(random.nextInt(CHAR_SOURCE.length())));
        }
        return sb.toString();
    }

    static String generateImage(SystemConfig config, String code) {
        int width = config.getCaptchaWidth();
        int height = config.getCaptchaHeight();
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        Random random = new Random();

        int startGray = 200 + random.nextInt(30);
        int endGray = 220 + random.nextInt(35);
        for (int y = 0; y < height; y++) {
            int gray = startGray + (endGray - startGray) * y / height;
            g.setColor(new Color(gray, gray, gray));
            g.drawLine(0, y, width, y);
        }

        int curveCount = 8;
        for (int i = 0; i < curveCount; i++) {
            QuadCurve2D curve = new QuadCurve2D.Float();
            int ctrlX = random.nextInt(width);
            int ctrlY = random.nextInt(height);
            int endX = random.nextInt(width);
            int endY = random.nextInt(height);
            curve.setCurve(0, random.nextInt(height), ctrlX, ctrlY, endX, endY);
            g.setColor(new Color(180 + random.nextInt(60), 180 + random.nextInt(60), 180 + random.nextInt(60)));
            g.draw(curve);
        }

        int noiseCount = 120;
        for (int i = 0; i < noiseCount; i++) {
            int x = random.nextInt(width);
            int y = random.nextInt(height);
            int size = random.nextInt(4) + 1;
            g.setColor(new Color(random.nextInt(180), random.nextInt(180), random.nextInt(180)));
            g.fillOval(x, y, size, size);
        }

        g.setFont(new Font("Arial", Font.BOLD | Font.ITALIC, 32));
        int charX = 16;
        for (int i = 0; i < code.length(); i++) {
            char c = code.charAt(i);
            AffineTransform transform = new AffineTransform();
            double theta = Math.toRadians(random.nextInt(40) - 20);
            transform.rotate(theta);
            int dx = random.nextInt(6) - 3;
            int dy = random.nextInt(8) - 4;
            transform.translate(charX + dx, height / 2 + 10 + dy);
            g.setTransform(transform);
            g.setColor(new Color(random.nextInt(80), random.nextInt(80), random.nextInt(80)));
            g.drawString(String.valueOf(c), 0, 0);
            charX += 28;
        }

        g.dispose();

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (IOException e) {
            throw new BusinessException("验证码图片生成失败");
        }
    }

    @Setter
    @Getter
    class VO {
        private String captchaImage;

        public VO() {
        }

        public VO(String captchaImage) {
            this.captchaImage = captchaImage;
        }

    }
}