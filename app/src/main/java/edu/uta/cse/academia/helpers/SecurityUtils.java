package edu.uta.cse.academia.helpers;

import java.security.MessageDigest;

/**
 * Created by Arun on 4/20/2016.
 */
public class SecurityUtils {

    public static final String md5(final String toEncrypt) {
        try {
            final MessageDigest digest = MessageDigest.getInstance("md5");
            digest.update(toEncrypt.getBytes());
            final byte[] bytes = digest.digest();
            final StringBuilder sb = new StringBuilder();
            for (int i = 0; i < bytes.length; i++) {
                sb.append(String.format("%02X", bytes[i]));
            }
            return sb.toString().toLowerCase();
        } catch (Exception exc) {
            exc.printStackTrace();
            return ""; // Impossibru!
        }
    }
}
