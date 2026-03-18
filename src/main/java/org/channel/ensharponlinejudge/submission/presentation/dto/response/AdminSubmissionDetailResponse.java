package org.channel.ensharponlinejudge.submission.presentation.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.channel.ensharponlinejudge.submission.domain.SubmissionStatus;

import lombok.Builder;

@Builder
public record AdminSubmissionDetailResponse(
    UUID submissionId,
    String repoUrl,
    SubmissionStatus status,
    int score,
    LocalDateTime submittedAt,
    List<TestDetailResponse> testDetails) {

  @Builder
  public record TestDetailResponse(
      String methodName,
      SubmissionStatus status,
      int durationMs,
      String message,
      boolean isHidden,
      int score) {}
}
