package org.channel.ensharponlinejudge.submission.service;

import org.channel.ensharponlinejudge.exception.BusinessException;
import org.channel.ensharponlinejudge.exception.enums.SubmissionErrorCode;
import org.channel.ensharponlinejudge.project.domain.Project;
import org.channel.ensharponlinejudge.project.repository.ProjectRepository;
import org.channel.ensharponlinejudge.submission.domain.Submission;
import org.channel.ensharponlinejudge.submission.infra.queue.SubmissionQueuePublisher;
import org.channel.ensharponlinejudge.submission.infra.queue.dto.SubmissionCreatedEvent;
import org.channel.ensharponlinejudge.submission.infra.queue.dto.SubmissionJudgeRequest;
import org.channel.ensharponlinejudge.submission.repository.SubmissionRepository;
import org.channel.ensharponlinejudge.user.domain.User;
import org.channel.ensharponlinejudge.user.repository.UserRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SubmissionCreatedEventListener {

  private final SubmissionRepository submissionRepository;
  private final ProjectRepository projectRepository;
  private final UserRepository userRepository;
  private final SubmissionQueuePublisher submissionQueuePublisher;

  @Transactional(propagation = Propagation.REQUIRES_NEW)
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

    User user =
        userRepository
            .findById(submission.getUserId())
            .orElseThrow(() -> new BusinessException(SubmissionErrorCode.USER_NOT_FOUND));

    SubmissionJudgeRequest submissionJudgeRequest =
        new SubmissionJudgeRequest(
            submission.getId(),
            submission.getUserId(),
            submission.getProjectId(),
            submission.getRepoUrl(),
            project.getTestCodeUrl(),
            project.getTimeLimit(),
            project.getMemoryLimit());
    try {
      submissionQueuePublisher.enqueue(submissionJudgeRequest);

      // 제출 상태 QUEUED로 변경
      submission.markQueued();
    } catch (Exception e) {

      // 제출 상태 SYSTEM_ERROR로 변경
      submission.markSystemError();
    }
  }
}
