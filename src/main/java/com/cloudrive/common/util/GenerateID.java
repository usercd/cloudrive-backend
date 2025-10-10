package com.cloudrive.common.util;

import java.security.SecureRandom;

/**
 * @author cd
 * @date 2025/10/10
 * @description
 */
public class GenerateID {
    public static String generateFileInfoRandomId(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(characters.charAt(random.nextInt(characters.length())));
        }
        return sb.toString();
    }
}
