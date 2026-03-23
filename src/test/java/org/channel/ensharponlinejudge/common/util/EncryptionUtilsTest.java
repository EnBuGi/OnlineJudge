package org.channel.ensharponlinejudge.common.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class EncryptionUtilsTest {

  private EncryptionUtils encryptionUtils;
  private final String secretKey = "test-secret-key-1234567890123456";

  @BeforeEach
  void setUp() {
    encryptionUtils = new EncryptionUtils(secretKey);
  }

  @Test
  @DisplayName("Encryption and decryption should return original string")
  void encryptDecryptTest() {
    String originalText = "gho_testToken123456789";
    String encryptedText = encryptionUtils.encrypt(originalText);

    assertThat(encryptedText).isNotEqualTo(originalText);

    String decryptedText = encryptionUtils.decrypt(encryptedText);
    assertThat(decryptedText).isEqualTo(originalText);
  }

  @Test
  @DisplayName(
      "Should return input as is if decryption fails or format is invalid (Migration support)")
  void decryptFallbackTest() {
    String plainText = "already_plaintext_token";
    String decrypted = encryptionUtils.decrypt(plainText);

    assertThat(decrypted).isEqualTo(plainText);
  }

  @Test
  @DisplayName("Should handle null input")
  void nullInputTest() {
    assertThat(encryptionUtils.encrypt(null)).isNull();
    assertThat(encryptionUtils.decrypt(null)).isNull();
  }
}
