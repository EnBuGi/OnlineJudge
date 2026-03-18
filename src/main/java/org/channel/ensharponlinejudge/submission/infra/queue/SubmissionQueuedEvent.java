package org.channel.ensharponlinejudge.submission.infra.queue;

import java.util.UUID;

public record SubmissionQueuedEvent(UUID submissionId) {}
