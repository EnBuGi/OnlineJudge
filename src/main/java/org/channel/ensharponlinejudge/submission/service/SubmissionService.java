package org.channel.ensharponlinejudge.submission.service;

import java.util.UUID;

import org.channel.ensharponlinejudge.exception.BusinessException;
import org.channel.ensharponlinejudge.exception.enums.SubmissionErrorCode;
import org.channel.ensharponlinejudge.project.domain.Project;
import org.channel.ensharponlinejudge.project.repository.ProjectRepository;
import org.channel.ensharponlinejudge.submission.repository.SubmissionRepository;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SubmissionService {

  private final ProjectRepository projectRepository;
  private final SubmissionRepository SubmissionRepository;

  public void submit(UUID userId, UUID projectId, String repoUrl) {

    Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new BusinessException(SubmissionErrorCode.PROJECT_NOT_FOUND));
  }
}
