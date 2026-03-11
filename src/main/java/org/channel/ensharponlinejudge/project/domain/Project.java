package org.channel.ensharponlinejudge.project.domain;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.*;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import lombok.AccessLevel;
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

  @Column(name = "test_code_url")
  private String testCodeUrl;

  @Column(name = "time_limit")
  private int timeLimit;

  @Column(name = "memory_limit")
  private int memoryLimit;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updated_at;
}
