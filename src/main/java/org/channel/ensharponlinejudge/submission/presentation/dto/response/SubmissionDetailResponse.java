package org.channel.ensharponlinejudge.submission.presentation.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.channel.ensharponlinejudge.submission.domain.SubmissionStatus;

public record SubmissionDetailResponse(
    UUID submissionId,
    String repoUrl,
    SubmissionStatus status,
    Integer score,
    Integer totalTests,
    Integer passedTests,
    LocalDateTime submittedAt,
    List<TestDetailResponse> details) {

  public record TestDetailResponse(
      String methodName,
      String status,
      Long durationMs,
      String message,
      boolean isHidden,
      Integer score) {}
}
