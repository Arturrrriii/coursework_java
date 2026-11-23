package metallurgy;

import java.security.MessageDigest;
import java.security.SecureRandom;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/** Хранение пароля: случайная соль и PBKDF2-HMAC-SHA256. */
public final class PasswordUtil {
    private PasswordUtil() {}
    public static byte[] salt() { byte[] s = new byte[16]; new SecureRandom().nextBytes(s); return s; }
    public static byte[] hash(char[] password, byte[] salt) throws Exception {
        PBEKeySpec spec = new PBEKeySpec(password, salt, 210000, 256);
        try { return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded(); }
        finally { spec.clearPassword(); }
    }
    public static boolean verify(char[] password, byte[] salt, byte[] expected) throws Exception {
        return MessageDigest.isEqual(hash(password, salt), expected);
    }
}
