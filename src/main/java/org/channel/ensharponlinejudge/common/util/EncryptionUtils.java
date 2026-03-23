package org.channel.ensharponlinejudge.common.util;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class EncryptionUtils {

  private static final String ALGORITHM = "AES/GCM/NoPadding";
  private static final int TAG_LENGTH_BIT = 128;
  private static final int IV_LENGTH_BYTE = 12;

  private final SecretKeySpec keySpec;

  public EncryptionUtils(
      @Value("${encryption.key:default-encryption-key-for-dev-only}") String secretKey) {
    // Ensure key is 32 bytes for AES-256
    byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
    byte[] paddedKey = new byte[32];
    System.arraycopy(keyBytes, 0, paddedKey, 0, Math.min(keyBytes.length, 32));
    this.keySpec = new SecretKeySpec(paddedKey, "AES");
  }

  public String encrypt(String strToEncrypt) {
    try {
      if (strToEncrypt == null) return null;

      byte[] iv = new byte[IV_LENGTH_BYTE];
      new SecureRandom().nextBytes(iv);

      Cipher cipher = Cipher.getInstance(ALGORITHM);
      GCMParameterSpec spec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);
      cipher.init(Cipher.ENCRYPT_MODE, keySpec, spec);

      byte[] cipherText = cipher.doFinal(strToEncrypt.getBytes(StandardCharsets.UTF_8));

      ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + cipherText.length);
      byteBuffer.put(iv);
      byteBuffer.put(cipherText);

      return Base64.getEncoder().encodeToString(byteBuffer.array());
    } catch (Exception e) {
      log.error("Encryption error", e);
      throw new RuntimeException("Encryption failed", e);
    }
  }

  public String decrypt(String strToDecrypt) {
    try {
      if (strToDecrypt == null) return null;

      byte[] decode = Base64.getDecoder().decode(strToDecrypt);
      ByteBuffer byteBuffer = ByteBuffer.wrap(decode);

      byte[] iv = new byte[IV_LENGTH_BYTE];
      byteBuffer.get(iv);

      byte[] cipherText = new byte[byteBuffer.remaining()];
      byteBuffer.get(cipherText);

      Cipher cipher = Cipher.getInstance(ALGORITHM);
      GCMParameterSpec spec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);
      cipher.init(Cipher.DECRYPT_MODE, keySpec, spec);

      return new String(cipher.doFinal(cipherText), StandardCharsets.UTF_8);
    } catch (IllegalArgumentException e) {
      // Probably not base64 encoded, return as is (for migration period or non-encrypted data)
      log.warn("Decrypt failed: Not a base64 string or wrong format. Returning input string.");
      return strToDecrypt;
    } catch (Exception e) {
      log.error("Decryption error", e);
      // If decryption fails, it might be plaintext from before encryption was introduced
      log.warn("Decryption failed. Returning input string as fallback.");
      return strToDecrypt;
    }
  }
}
