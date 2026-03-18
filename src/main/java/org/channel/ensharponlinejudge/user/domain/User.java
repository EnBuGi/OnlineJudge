package org.channel.ensharponlinejudge.user.domain;

import java.util.UUID;

import jakarta.persistence.*;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.type.SqlTypes;

import lombok.*;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLDelete(sql = "UPDATE users SET is_deleted = true WHERE id = ?")
@SQLRestriction("is_deleted = false")
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @JdbcTypeCode(SqlTypes.VARCHAR)
  private UUID id;

  @Column(name = "github_id", nullable = false, length = 50, unique = true)
  private String githubId;

  @Column(nullable = false, length = 20)
  private String name;

  @Column(nullable = false)
  private int generation;

  @Enumerated(EnumType.STRING)
  private Role role;

  @Column(name = "profile_image_url", nullable = false, length = 500)
  private String profileImageUrl;

  @Column(nullable = false)
  private boolean isDeleted = false;

  @Builder
  private User(
      UUID id,
      String githubId,
      String name,
      int generation,
      Role role,
      String profileImageUrl,
      boolean isDeleted) {
    this.id = id;
    this.githubId = githubId;
    this.name = name;
    this.generation = generation;
    this.role = role;
    this.profileImageUrl = profileImageUrl;
    this.isDeleted = isDeleted;
  }

  public static User initialize(
      String githubId, String name, int generation, String profileImageUrl) {
    return User.builder()
        .githubId(githubId)
        .name(name)
        .generation(generation)
        .role(Role.MENTEE)
        .profileImageUrl(profileImageUrl)
        .isDeleted(false)
        .build();
  }
}
