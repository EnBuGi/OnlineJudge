package org.channel.ensharponlinejudge.domain.member.entity;

import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import jakarta.persistence.*;

import org.channel.ensharponlinejudge.exception.BusinessException;
import org.channel.ensharponlinejudge.exception.enums.AuthErrorCode;

import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {

  private static final Pattern BCRYPT_PATTERN = Pattern.compile("^\\$2[aby]\\$.{56}$");

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private Long id;

  @Column(nullable = false, unique = true)
  private String email;

  @Column(nullable = false)
  private String password;

  @ElementCollection(fetch = FetchType.EAGER)
  private List<String> roles;

  @Builder
  private Member(Long id, String email, String password, List<String> roles) {
    validatePassword(password);
    this.id = id;
    this.email = email;
    this.password = password;
    this.roles = roles;
  }

  public static Member initialize(String email, String password) {
    return Member.builder()
        .email(email)
        .password(password)
        .roles(Collections.singletonList("ROLE_USER"))
        .build();
  }

  private void validatePassword(String password) {
    if (password == null || !BCRYPT_PATTERN.matcher(password).matches()) {
      throw new BusinessException(AuthErrorCode.INVALID_PASSWORD_FORMAT);
    }
  }
}
