package org.channel.ensharponlinejudge.submission.service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.channel.ensharponlinejudge.exception.BusinessException;
import org.channel.ensharponlinejudge.exception.enums.SubmissionErrorCode;
import org.channel.ensharponlinejudge.project.domain.Project;
import org.channel.ensharponlinejudge.project.repository.ProjectRepository;
import org.channel.ensharponlinejudge.submission.domain.Submission;
import org.channel.ensharponlinejudge.submission.domain.SubmissionResultDetail;
import org.channel.ensharponlinejudge.submission.domain.SubmissionStatus;
import org.channel.ensharponlinejudge.submission.infra.queue.dto.SubmissionCreatedEvent;
import org.channel.ensharponlinejudge.submission.presentation.dto.response.MyGlobalSubmissionResponse;
import org.channel.ensharponlinejudge.submission.presentation.dto.response.SubmissionDetailResponse;
import org.channel.ensharponlinejudge.submission.presentation.dto.response.SubmissionHistoryResponse;
import org.channel.ensharponlinejudge.submission.repository.SubmissionRepository;
import org.channel.ensharponlinejudge.submission.repository.SubmissionResultDetailRepository;
import org.channel.ensharponlinejudge.user.repository.UserRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubmissionService {

  private final ProjectRepository projectRepository;
  private final UserRepository userRepository;
  private final SubmissionRepository submissionRepository;
  private final SubmissionResultDetailRepository submissionResultDetailRepository;
  private final ApplicationEventPublisher applicationEventPublisher;

  @Transactional
  public UUID submit(UUID userId, UUID projectId, String repoUrl) {

    List<SubmissionStatus> inProgress =
        List.of(SubmissionStatus.ENQUEUING, SubmissionStatus.QUEUED, SubmissionStatus.PROCESSING);

    Project project =
        projectRepository
            .findById(projectId)
            .orElseThrow(() -> new BusinessException(SubmissionErrorCode.PROJECT_NOT_FOUND));

    if (project.getTestCodeUrl() == null || project.getTestCodeUrl().isBlank()) {
      throw new BusinessException(SubmissionErrorCode.TEST_CASE_NOT_FOUND);
    }

    userRepository
        .findById(userId)
        .orElseThrow(() -> new BusinessException(SubmissionErrorCode.USER_NOT_FOUND));

    if (submissionRepository.existsByUserIdAndProjectIdAndStatusIn(userId, projectId, inProgress)) {
      throw new BusinessException(SubmissionErrorCode.SUBMISSION_IN_PROGRESS);
    }

    Submission submission = Submission.initialize(userId, projectId, repoUrl);
    Submission savedSubmission = submissionRepository.save(submission);

    // 내부 이벤트 발행
    applicationEventPublisher.publishEvent(new SubmissionCreatedEvent(savedSubmission.getId()));

    return savedSubmission.getId();
  }

  @Transactional(readOnly = true)
  public Page<SubmissionHistoryResponse> getSubmissionHistory(
      UUID userId, UUID projectId, Pageable pageable) {
    projectRepository
        .findById(projectId)
        .orElseThrow(() -> new BusinessException(SubmissionErrorCode.PROJECT_NOT_FOUND));

    userRepository
        .findById(userId)
        .orElseThrow(() -> new BusinessException(SubmissionErrorCode.USER_NOT_FOUND));

    Page<Submission> submissions =
        submissionRepository.findByUserIdAndProjectIdOrderBySubmittedAtDesc(
            userId, projectId, pageable);

    return submissions.map(
        submission ->
            new SubmissionHistoryResponse(
                submission.getId(),
                submission.getRepoUrl(),
                submission.getStatus(),
                submission.getScore(),
                submission.getSubmittedAt()));
  }

  @Transactional
  public void cancelSubmission(UUID userId, UUID submissionId) {
    Submission submission =
        submissionRepository
            .findById(submissionId)
            .orElseThrow(() -> new BusinessException(SubmissionErrorCode.SUBMISSION_NOT_FOUND));

    if (!submission.getUserId().equals(userId)) {
      throw new BusinessException(SubmissionErrorCode.SUBMISSION_NOT_FOUND);
    }

    List<SubmissionStatus> cancellable =
        List.of(SubmissionStatus.ENQUEUING, SubmissionStatus.QUEUED, SubmissionStatus.PROCESSING);

    if (!cancellable.contains(submission.getStatus())) {
      throw new BusinessException(SubmissionErrorCode.SUBMISSION_NOT_FOUND);
    }

    submission.markCancelled();
  }

  @Transactional(readOnly = true)
  public SubmissionDetailResponse getSubmissionDetail(UUID userId, UUID submissionId) {
    log.info(
        "[SubmissionService] Getting submission detail: submissionId={}, userId={}",
        submissionId,
        userId);

    Submission submission =
        submissionRepository
            .findById(submissionId)
            .orElseThrow(
                () -> {
                  log.warn("[SubmissionService] Submission not found: {}", submissionId);
                  return new BusinessException(SubmissionErrorCode.SUBMISSION_NOT_FOUND);
                });

    if (!submission.getUserId().equals(userId)) {
      log.warn(
          "[SubmissionService] User mismatch: expected={}, actual={}",
          submission.getUserId(),
          userId);
      throw new BusinessException(SubmissionErrorCode.SUBMISSION_NOT_FOUND);
    }

    log.debug("[SubmissionService] Fetching result details for submission: {}", submissionId);
    List<SubmissionResultDetail> details =
        submissionResultDetailRepository.findBySubmissionId(submissionId);
    log.debug("[SubmissionService] Found {} result details", details.size());

    List<SubmissionDetailResponse.TestDetailResponse> testDetails =
        details.stream()
            .map(
                d -> {
                  try {
                    return new SubmissionDetailResponse.TestDetailResponse(
                        d.isHidden() ? "히든 테스트 케이스" : d.getMethodName(),
                        d.getStatus().name(),
                        (long) d.getDurationMs(),
                        d.isHidden() ? null : d.getMessage(),
                        d.isHidden(),
                        d.getScore());
                  } catch (Exception e) {
                    log.error(
                        "[SubmissionService] Error mapping result detail: detailId={}, error={}",
                        d.getId(),
                        e.getMessage());
                    throw e;
                  }
                })
            .toList();

    log.info(
        "[SubmissionService] Successfully constructed SubmissionDetailResponse for {}",
        submissionId);
    return new SubmissionDetailResponse(
        submission.getId(),
        submission.getRepoUrl(),
        submission.getStatus(),
        submission.getScore(),
        submission.getTotalTests(),
        submission.getPassedTests(),
        submission.getSubmittedAt(),
        testDetails);
  }

  @Transactional(readOnly = true)
  public Page<MyGlobalSubmissionResponse> getMyGlobalSubmissions(UUID userId, Pageable pageable) {
    Page<Submission> submissions = submissionRepository.findByUserId(userId, pageable);

    Set<UUID> projectIds =
        submissions.stream().map(Submission::getProjectId).collect(Collectors.toSet());

    Map<UUID, Project> projectMap =
        projectRepository.findAllById(projectIds).stream()
            .collect(Collectors.toMap(Project::getId, p -> p));

    List<MyGlobalSubmissionResponse> content =
        submissions.stream()
            .map(
                s -> {
                  Project project = projectMap.get(s.getProjectId());
                  return MyGlobalSubmissionResponse.builder()
                      .submissionId(s.getId())
                      .projectId(s.getProjectId())
                      .submittedAt(s.getSubmittedAt())
                      .problemTitle(project != null ? project.getTitle() : "Unknown")
                      .status(s.getStatus())
                      .score(s.getScore())
                      .memoryUsage(s.getMemoryUsage())
                      .timeUsage(s.getTimeUsage())
                      .build();
                })
            .collect(Collectors.toList());

    return new PageImpl<>(content, pageable, submissions.getTotalElements());
  }
}
