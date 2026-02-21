package org.channel.ensharponlinejudge.submission.infra.queue.dto;

import java.util.UUID;

public record SubmissionQueueEvent(
        UUID submissionId
) {
}
