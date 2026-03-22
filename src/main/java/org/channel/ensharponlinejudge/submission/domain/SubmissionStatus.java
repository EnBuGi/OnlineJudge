package org.channel.ensharponlinejudge.submission.domain;

public enum SubmissionStatus {
  ENQUEUING,
  QUEUED,
  PROCESSING,
  COMPLETED,
  COMPILE_ERROR,
  RUNTIME_ERROR,
  WRONG_ANSWER,
  TIME_LIMIT_EXCEEDED,
  MEMORY_LIMIT_EXCEEDED,
  SYSTEM_ERROR,
  CANCELLED
}
