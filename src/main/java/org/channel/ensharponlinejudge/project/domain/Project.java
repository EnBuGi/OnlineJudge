package org.channel.ensharponlinejudge.project.domain;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.*;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "projects")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Project {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @JdbcTypeCode(SqlTypes.VARCHAR)
  private UUID id;

  @Column(nullable = false)
  private int generation;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private ProjectType type;

  @Column(nullable = false)
  private String title;

  @Column(columnDefinition = "Text")
  private String description;

  @Column(name = "start_date", nullable = false)
  private LocalDateTime startDate;

  @Column(name = "due_date", nullable = false)
  private LocalDateTime dueDate;

  @Column(name = "skeleton_url")
  private String skeletonUrl;

  @Column(name = "test_code_key")
  private String testCodeKey;

  @Column(name = "time_limit")
  private Integer timeLimit;

  @Column(name = "memory_limit")
  private Integer memoryLimit;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updated_at;

  @Builder
  private Project(
      int generation,
      ProjectType type,
      String title,
      String description,
      LocalDateTime startDate,
      LocalDateTime dueDate,
      String skeletonUrl,
      String testCodeKey,
      Integer timeLimit,
      Integer memoryLimit) {
    this.generation = generation;
    this.type = type;
    this.title = title;
    this.description = description;
    this.startDate = startDate;
    this.dueDate = dueDate;
    this.skeletonUrl = skeletonUrl;
    this.testCodeKey = testCodeKey;
    this.timeLimit = timeLimit;
    this.memoryLimit = memoryLimit;
  }

  public static Project initialize(
      int generation,
      ProjectType type,
      String title,
      String description,
      LocalDateTime startDate,
      LocalDateTime dueDate,
      String skeletonUrl,
      String testCodeKey,
      Integer timeLimit,
      Integer memoryLimit) {
    return Project.builder()
        .generation(generation)
        .type(type)
        .title(title)
        .description(description)
        .startDate(startDate)
        .dueDate(dueDate)
        .skeletonUrl(skeletonUrl)
        .testCodeKey(testCodeKey)
        .timeLimit(timeLimit)
        .memoryLimit(memoryLimit)
        .build();
  }

  public void update(
      int generation,
      ProjectType type,
      String title,
      String description,
      LocalDateTime startDate,
      LocalDateTime dueDate,
      String skeletonUrl,
      Integer timeLimit,
      Integer memoryLimit) {
    this.generation = generation;
    this.type = type;
    this.title = title;
    this.description = description;
    this.startDate = startDate;
    this.dueDate = dueDate;
    this.skeletonUrl = skeletonUrl;
    this.timeLimit = timeLimit;
    this.memoryLimit = memoryLimit;
  }

  /** OCI 오브젝트 스토리지에 업로드된 테스트 코드의 Key값을 저장합니다. */
  public void updateTestCodeKey(String testCodeKey) {
    this.testCodeKey = testCodeKey;
  }
}
