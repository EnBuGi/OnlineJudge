package org.channel.ensharponlinejudge.submission.infra.queue.dto;

import java.util.UUID;

public record SubmissionJudgeRequest(
        UUID submissionId,
        UUID userId,
        UUID projectId,
        String repoUrl,
        int timeLimit,
        int memoryLimit
) {}
