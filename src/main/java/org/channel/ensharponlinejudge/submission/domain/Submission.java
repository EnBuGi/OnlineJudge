package org.channel.ensharponlinejudge.submission.domain;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "submissions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Submission {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @JdbcTypeCode(SqlTypes.VARCHAR)
  private UUID id;

  @Column(name = "user_id", nullable = false)
  @JdbcTypeCode(SqlTypes.VARCHAR)
  private UUID userId;

  @Column(name = "project_id", nullable = false)
  @JdbcTypeCode(SqlTypes.VARCHAR)
  private UUID projectId;

  @Column(name = "repo_url", length = 255, nullable = false)
  private String repoUrl;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", length = 20)
  private SubmissionStatus status;

  @Column private Integer score;

  @CreationTimestamp
  @Column(name = "submit_at", nullable = false)
  private LocalDateTime submittedAt;

  @Column(name = "time_usage")
  private Integer timeUsage;

  @Column(name = "memory_usage")
  private Integer memoryUsage;

  @Column(name = "total_tests")
  private Integer totalTests;

  @Column(name = "passed_tests")
  private Integer passedTests;

  @Builder
  private Submission(UUID userId, UUID projectId, String repoUrl) {
    this.userId = userId;
    this.projectId = projectId;
    this.repoUrl = repoUrl;
    this.status = SubmissionStatus.ENQUEUING;
  }

  public static Submission initialize(UUID userId, UUID projectId, String repoUrl) {

    return Submission.builder().userId(userId).projectId(projectId).repoUrl(repoUrl).build();
  }

  public void markQueued() {
    this.status = SubmissionStatus.QUEUED;
  }

  public void markProcessing() {
    this.status = SubmissionStatus.PROCESSING;
  }

  public void markCompleted(int score, int totalTests, int passedTests) {
    this.status = SubmissionStatus.COMPLETED;
    this.score = score;
    this.totalTests = totalTests;
    this.passedTests = passedTests;
  }

  public void markSystemError() {
    this.status = SubmissionStatus.SYSTEM_ERROR;
  }

  public void markCancelled() {
    this.status = SubmissionStatus.CANCELLED;
  }
}
