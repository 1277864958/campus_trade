package com.campus.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class CaptchaService {

    private final StringRedisTemplate redis;

    private static final String CAPTCHA_PREFIX = "captcha:";
    private static final int WIDTH = 120;
    private static final int HEIGHT = 40;
    private static final int CODE_LEN = 4;
    private static final long EXPIRE_MIN = 5L;
    private static final String CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789";

    public record CaptchaResult(String captchaId, String base64Image) {}

    public CaptchaResult generate() {
        String code = randomCode();
        String captchaId = UUID.randomUUID().toString().replace("-", "");
        redis.opsForValue().set(CAPTCHA_PREFIX + captchaId, code.toLowerCase(), EXPIRE_MIN, TimeUnit.MINUTES);
        String base64 = "data:image/png;base64," + drawImage(code);
        return new CaptchaResult(captchaId, base64);
    }

    public boolean verify(String captchaId, String inputCode) {
        if (captchaId == null || inputCode == null) return false;
        String key = CAPTCHA_PREFIX + captchaId;
        String stored = redis.opsForValue().get(key);
        redis.delete(key);
        return stored != null && stored.equals(inputCode.toLowerCase());
    }

    private String randomCode() {
        Random rnd = new Random();
        StringBuilder sb = new StringBuilder(CODE_LEN);
        for (int i = 0; i < CODE_LEN; i++) {
            sb.append(CHARS.charAt(rnd.nextInt(CHARS.length())));
        }
        return sb.toString();
    }

    private String drawImage(String code) {
        BufferedImage img = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        Random rnd = new Random();

        g.setColor(new Color(245, 245, 245));
        g.fillRect(0, 0, WIDTH, HEIGHT);

        for (int i = 0; i < 6; i++) {
            g.setColor(new Color(rnd.nextInt(200), rnd.nextInt(200), rnd.nextInt(200)));
            g.drawLine(rnd.nextInt(WIDTH), rnd.nextInt(HEIGHT), rnd.nextInt(WIDTH), rnd.nextInt(HEIGHT));
        }

        g.setFont(new Font("Arial", Font.BOLD, 28));
        for (int i = 0; i < code.length(); i++) {
            g.setColor(new Color(rnd.nextInt(150), rnd.nextInt(150), rnd.nextInt(150)));
            double theta = (rnd.nextDouble() - 0.5) * 0.4;
            g.rotate(theta, 20 + i * 25, 28);
            g.drawString(String.valueOf(code.charAt(i)), 12 + i * 25, 30);
            g.rotate(-theta, 20 + i * 25, 28);
        }

        for (int i = 0; i < 30; i++) {
            g.setColor(new Color(rnd.nextInt(255), rnd.nextInt(255), rnd.nextInt(255)));
            g.fillRect(rnd.nextInt(WIDTH), rnd.nextInt(HEIGHT), 1, 1);
        }

        g.dispose();
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(img, "png", baos);
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
