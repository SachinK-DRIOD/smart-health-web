package com.smarthealth.util;

import org.mindrot.jbcrypt.BCrypt;

/** BCrypt hashing: salted, slow by design. Plain-text passwords are never stored. */
public final class PasswordUtil {
    private PasswordUtil() {}

    public static String hash(String plain) { return BCrypt.hashpw(plain, BCrypt.gensalt(12)); }

    public static boolean verify(String plain, String hash) {
        try { return plain != null && hash != null && BCrypt.checkpw(plain, hash); }
        catch (IllegalArgumentException e) { return false; }
    }

    /** 8 to 72 characters (BCrypt's limit), with at least one letter and one digit. */
    public static boolean isStrong(String pw) {
        return pw != null && pw.length() >= 8 && pw.length() <= 72
                && pw.matches(".*[A-Za-z].*") && pw.matches(".*\\d.*");
    }
}
