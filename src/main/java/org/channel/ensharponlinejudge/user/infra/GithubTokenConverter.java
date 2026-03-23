package org.channel.ensharponlinejudge.user.infra;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import org.channel.ensharponlinejudge.common.util.EncryptionUtils;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Converter
@Component
@RequiredArgsConstructor
public class GithubTokenConverter implements AttributeConverter<String, String> {

  private final EncryptionUtils encryptionUtils;

  @Override
  public String convertToDatabaseColumn(String attribute) {
    if (attribute == null) return null;
    return encryptionUtils.encrypt(attribute);
  }

  @Override
  public String convertToEntityAttribute(String dbData) {
    if (dbData == null) return null;
    return encryptionUtils.decrypt(dbData);
  }
}
