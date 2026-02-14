package org.channel.ensharponlinejudge.submission.service;

import java.util.List;
import java.util.UUID;

import org.channel.ensharponlinejudge.exception.BusinessException;
import org.channel.ensharponlinejudge.exception.enums.SubmissionErrorCode;
import org.channel.ensharponlinejudge.project.domain.Project;
import org.channel.ensharponlinejudge.project.repository.ProjectRepository;
import org.channel.ensharponlinejudge.submission.domain.SubmissionStatus;
import org.channel.ensharponlinejudge.submission.repository.SubmissionRepository;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SubmissionService {

  private final ProjectRepository projectRepository;
  private final SubmissionRepository submissionRepository;

  public void submit(UUID userId, UUID projectId, String repoUrl) {

    List<SubmissionStatus> inProgress =
        List.of(SubmissionStatus.QUEUED, SubmissionStatus.PROCESSING);

    Project project =
        projectRepository
            .findById(projectId)
            .orElseThrow(() -> new BusinessException(SubmissionErrorCode.PROJECT_NOT_FOUND));

    if (submissionRepository.existsByUserIdAndProjectIdAndStatusIn(userId, projectId, inProgress)) {
      throw new BusinessException(SubmissionErrorCode.SUBMISSION_IN_PROGRESS);
    }
  }
}
