package org.channel.ensharponlinejudge.submission.presentation.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import org.channel.ensharponlinejudge.submission.domain.SubmissionStatus;

public record SubmissionHistoryResponse(
    UUID submissionId,
    String repoUrl,
    SubmissionStatus status,
    Integer score,
    LocalDateTime submittedAt) {}
