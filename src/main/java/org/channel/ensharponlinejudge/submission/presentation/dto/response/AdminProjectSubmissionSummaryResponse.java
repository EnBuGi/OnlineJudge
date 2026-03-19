package org.channel.ensharponlinejudge.submission.presentation.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import org.channel.ensharponlinejudge.submission.domain.SubmissionStatus;

import lombok.Builder;

@Builder
public record AdminProjectSubmissionSummaryResponse(
    UUID userId,
    UUID submissionId,
    String name,
    String githubId,
    SubmissionStatus status,
    Integer score,
    LocalDateTime lastSubmittedAt) {}
