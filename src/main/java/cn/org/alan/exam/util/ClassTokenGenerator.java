package cn.org.alan.exam.util;

import java.security.SecureRandom;

/**
 * @Author Alan
 * @Version
 * @Date 2024/3/28 1:53 PM
 */
public class ClassTokenGenerator {

    private static final String CHAR_POOL = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    public static String generateClassToken(int length) {
        SecureRandom random = new SecureRandom();
        StringBuilder tokenBuilder = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            tokenBuilder.append(CHAR_POOL.charAt(random.nextInt(CHAR_POOL.length())));
        }

        return tokenBuilder.toString();
    }


}