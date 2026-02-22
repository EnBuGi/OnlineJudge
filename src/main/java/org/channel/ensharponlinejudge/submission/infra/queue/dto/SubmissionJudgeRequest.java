package org.channel.ensharponlinejudge.submission.infra.queue.dto;

import java.util.UUID;

public record SubmissionJudgeRequest(
        UUID submissionId,
        UUID userId,
        UUID projectId,
        String repoUrl,
        String testCodeGetUrl,
        int timeLimit,
        int memoryLimit
) {}
