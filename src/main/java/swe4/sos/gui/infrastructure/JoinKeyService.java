package swe4.sos.gui.infrastructure;

import java.security.SecureRandom;
import java.util.Base64;

public class JoinKeyService {
  private static final SecureRandom random = new SecureRandom();
  private static final Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();
  private static final int KEY_LENGTH = 12; // 9 bytes → 12 chars base64

  public static String generateJoinKey() {
    byte[] buffer = new byte[9]; // 9 bytes gives 12 chars in base64
    random.nextBytes(buffer);
    return encoder.encodeToString(buffer);
  }

  public static boolean isValidJoinKey(String key) {
    if (key == null || key.length() != KEY_LENGTH) {
      return false;
    }
    try {
      Base64.getUrlDecoder().decode(key);
      return true;
    } catch (IllegalArgumentException e) {
      return false;
    }
  }
}
