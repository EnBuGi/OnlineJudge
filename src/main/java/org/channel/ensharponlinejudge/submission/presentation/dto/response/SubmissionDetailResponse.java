package org.channel.ensharponlinejudge.submission.presentation.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.channel.ensharponlinejudge.submission.domain.SubmissionStatus;
import org.channel.ensharponlinejudge.submission.domain.TestStatus;

public record SubmissionDetailResponse(
    UUID submissionId,
    String repoUrl,
    SubmissionStatus status,
    Integer score,
    Integer totalTests,
    Integer passedTests,
    String projectType,
    LocalDateTime submittedAt,
    List<TestDetailResponse> details) {

  public record TestDetailResponse(
      String methodName,
      TestStatus status,
      Integer durationMs,
      String message,
      boolean isHidden,
      Integer score) {}
}
