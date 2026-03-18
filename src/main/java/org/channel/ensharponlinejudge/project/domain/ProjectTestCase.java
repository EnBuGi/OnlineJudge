package org.channel.ensharponlinejudge.project.domain;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "project_test_cases")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProjectTestCase {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @JdbcTypeCode(SqlTypes.VARCHAR)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "project_id", nullable = false)
  private Project project;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private int score;

  @Column(name = "is_hidden", nullable = false)
  private boolean isHidden;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  @Builder
  private ProjectTestCase(Project project, String name, int score, boolean isHidden) {
    this.project = project;
    this.name = name;
    this.score = score;
    this.isHidden = isHidden;
  }

  public static ProjectTestCase initialize(
      Project project, String name, int score, boolean isHidden) {
    return ProjectTestCase.builder()
        .project(project)
        .name(name)
        .score(score)
        .isHidden(isHidden)
        .build();
  }
}
