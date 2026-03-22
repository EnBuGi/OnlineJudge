package org.channel.ensharponlinejudge.submission.presentation.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.channel.ensharponlinejudge.submission.domain.SubmissionStatus;
import org.channel.ensharponlinejudge.submission.domain.TestStatus;

import lombok.Builder;

@Builder
public record AdminSubmissionDetailResponse(
    UUID submissionId,
    String userName,
    String githubId,
    String repoUrl,
    SubmissionStatus status,
    Integer score,
    Integer memoryUsage,
    Integer timeExecution,
    String projectType,
    LocalDateTime submittedAt,
    String sourceCode,
    List<TestDetailResponse> testDetails) {

  @Builder
  public record TestDetailResponse(
      String methodName,
      TestStatus status,
      Integer durationMs,
      String message,
      boolean isHidden,
      Integer score) {}
}
