package org.channel.ensharponlinejudge.submission.presentation.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import org.channel.ensharponlinejudge.submission.domain.SubmissionStatus;

import lombok.Builder;

@Builder
public record AdminGlobalSubmissionResponse(
    UUID submissionId,
    UUID projectId,
    LocalDateTime submittedAt,
    String githubId,
    String name,
    String problemTitle,
    String projectType,
    SubmissionStatus status,
    Integer score,
    Integer memoryUsage,
    Integer timeUsage) {}
