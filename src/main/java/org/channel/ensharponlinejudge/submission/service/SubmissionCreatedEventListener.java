package org.channel.ensharponlinejudge.submission.service;

import org.channel.ensharponlinejudge.exception.BusinessException;
import org.channel.ensharponlinejudge.exception.enums.SubmissionErrorCode;
import org.channel.ensharponlinejudge.project.domain.Project;
import org.channel.ensharponlinejudge.project.infra.storage.ObjectStorageService;
import org.channel.ensharponlinejudge.project.repository.ProjectRepository;
import org.channel.ensharponlinejudge.submission.domain.Submission;
import org.channel.ensharponlinejudge.submission.infra.queue.SubmissionQueuePublisher;
import org.channel.ensharponlinejudge.submission.infra.queue.dto.SubmissionCreatedEvent;
import org.channel.ensharponlinejudge.submission.infra.queue.dto.SubmissionJudgeRequest;
import org.channel.ensharponlinejudge.submission.repository.SubmissionRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubmissionCreatedEventListener {

  private final SubmissionRepository submissionRepository;
  private final ProjectRepository projectRepository;
  private final SubmissionQueuePublisher submissionQueuePublisher;
  private final ObjectStorageService objectStorageService;

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void onSubmissionQueued(SubmissionCreatedEvent submissionCreatedEvent) {

    Submission submission =
        submissionRepository
            .findById(submissionCreatedEvent.submissionId())
            .orElseThrow(() -> new BusinessException(SubmissionErrorCode.SUBMISSION_NOT_FOUND));

    Project project =
        projectRepository
            .findById(submission.getProjectId())
            .orElseThrow(() -> new BusinessException(SubmissionErrorCode.PROJECT_NOT_FOUND));

    // Generate fresh PAR URL for test code (to avoid expiration 404)
    String testCodeKey = project.getTestCodeKey();
    if (testCodeKey == null || testCodeKey.isBlank()) {
      throw new BusinessException(SubmissionErrorCode.TEST_CASE_NOT_FOUND);
    }
    String freshTestCodeUrl = objectStorageService.generateGetUrl(testCodeKey);
    log.info("[SubmissionCreatedEventListener] Generated fresh testCodeUrl: {}", freshTestCodeUrl);

    SubmissionJudgeRequest submissionJudgeRequest =
        new SubmissionJudgeRequest(
            submission.getId(),
            submission.getUserId(),
            submission.getProjectId(),
            submission.getRepoUrl(),
            freshTestCodeUrl,
            project.getTimeLimit(),
            project.getMemoryLimit(),
            project.getType().name());

    try {
      log.info(
          "Enqueuing submission: submissionId={}, userId={}, projectId={}",
          submission.getId(),
          submission.getUserId(),
          submission.getProjectId());

      submissionQueuePublisher.enqueue(submissionJudgeRequest);

      // 제출 상태 QUEUED로 변경
      submission.markQueued();
      log.info("Successfully enqueued submission: submissionId={}", submission.getId());
    } catch (Exception e) {
      log.error(
          "Failed to enqueue submission: submissionId={}, error={}",
          submission.getId(),
          e.getMessage(),
          e);
      // 제출 상태 SYSTEM_ERROR로 변경
      submission.markSystemError();
    }
  }
}
