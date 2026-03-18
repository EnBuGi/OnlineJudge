package org.channel.ensharponlinejudge.submission.domain;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "submission_result_details")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SubmissionResultDetail {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @JdbcTypeCode(SqlTypes.VARCHAR)
  private UUID id;

  @Column(name = "submission_id", nullable = false)
  @JdbcTypeCode(SqlTypes.VARCHAR)
  private UUID submissionId;

  @Column(name = "method_name", nullable = false)
  private String methodName;

  @Column(name = "status", nullable = false, length = 30)
  private String status;

  @Column(name = "duration_ms")
  private Long durationMs;

  @Column(name = "message", columnDefinition = "TEXT")
  private String message;

  @Column(name = "is_hidden", nullable = false)
  private boolean isHidden;

  @Column(name = "score")
  private Integer score;

  @Builder
  public SubmissionResultDetail(
      UUID submissionId,
      String methodName,
      String status,
      Long durationMs,
      String message,
      boolean isHidden,
      Integer score) {
    this.submissionId = submissionId;
    this.methodName = methodName;
    this.status = status;
    this.durationMs = durationMs;
    this.message = message;
    this.isHidden = isHidden;
    this.score = score;
  }
}
