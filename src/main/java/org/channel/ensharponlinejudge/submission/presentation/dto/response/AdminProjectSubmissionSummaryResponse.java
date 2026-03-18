package org.channel.ensharponlinejudge.submission.presentation.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import org.channel.ensharponlinejudge.submission.domain.SubmissionStatus;

import lombok.Builder;

@Builder
public record AdminProjectSubmissionSummaryResponse(
    UUID userId,
    String name,
    String githubId,
    SubmissionStatus status,
    int score,
    LocalDateTime lastSubmittedAt) {}
