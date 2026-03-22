package org.channel.ensharponlinejudge.submission.presentation.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import org.channel.ensharponlinejudge.submission.domain.SubmissionStatus;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminUserProjectSubmissionResponse {
  private UUID submissionId;
  private SubmissionStatus status;
  private Integer score;
  private Integer memoryUsage;
  private Integer timeExecution;
  private String projectType;
  private LocalDateTime submittedAt;
}
