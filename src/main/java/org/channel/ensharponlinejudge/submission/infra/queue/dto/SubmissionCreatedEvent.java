package org.channel.ensharponlinejudge.submission.infra.queue.dto;

import java.util.UUID;

public record SubmissionCreatedEvent(
        UUID submissionId
) {
}
