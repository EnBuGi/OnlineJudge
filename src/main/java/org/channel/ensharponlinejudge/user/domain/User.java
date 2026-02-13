package org.channel.ensharponlinejudge.user.domain;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

import jakarta.persistence.*;

import org.channel.ensharponlinejudge.exception.BusinessException;
import org.channel.ensharponlinejudge.exception.enums.AuthErrorCode;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.type.SqlTypes;

import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLDelete(
    sql = "UPDATE member SET is_deleted = true, email = CONCAT(email, '-del-', id) WHERE id = ?")
@SQLRestriction("is_deleted = false")
public class User {

  private static final Pattern BCRYPT_PATTERN = Pattern.compile("^\\$2[aby]\\$.{56}$");

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @JdbcTypeCode(SqlTypes.VARCHAR)
  private UUID id;

  @Column(nullable = false, unique = true)
  private String email;

  @Column(nullable = false)
  private String password;

  @ElementCollection
  @Enumerated(EnumType.STRING)
  private List<Role> roles;

  @Column(nullable = false)
  private boolean isDeleted = false;

  @Builder
  private User(UUID id, String email, String password, List<Role> roles, boolean isDeleted) {
    validatePassword(password);
    this.id = id;
    this.email = email;
    this.password = password;
    this.roles = roles;
    this.isDeleted = isDeleted;
  }

  public static User initialize(String email, String password) {
    return User.builder()
        .email(email)
        .password(password)
        .roles(Collections.singletonList(Role.ROLE_MENTEE))
        .isDeleted(false)
        .build();
  }

  private void validatePassword(String password) {
    if (password == null || !BCRYPT_PATTERN.matcher(password).matches()) {
      throw new BusinessException(AuthErrorCode.INVALID_PASSWORD_FORMAT);
    }
  }
}
