package org.channel.ensharponlinejudge.domain.member.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Collections;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private String email;

  @Column(nullable = false)
  private String password;

  @ElementCollection(fetch = FetchType.EAGER)
  private List<String> roles;

  @Builder
  private Member(Long id, String email, String password, List<String> roles) {
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
}
