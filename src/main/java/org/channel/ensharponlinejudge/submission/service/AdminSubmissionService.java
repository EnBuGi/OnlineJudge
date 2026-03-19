package org.channel.ensharponlinejudge.submission.service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.channel.ensharponlinejudge.exception.BusinessException;
import org.channel.ensharponlinejudge.exception.enums.ProjectErrorCode;
import org.channel.ensharponlinejudge.exception.enums.SubmissionErrorCode;
import org.channel.ensharponlinejudge.project.domain.Project;
import org.channel.ensharponlinejudge.project.repository.ProjectRepository;
import org.channel.ensharponlinejudge.submission.domain.Submission;
import org.channel.ensharponlinejudge.submission.domain.SubmissionResultDetail;
import org.channel.ensharponlinejudge.submission.presentation.dto.response.AdminGlobalSubmissionResponse;
import org.channel.ensharponlinejudge.submission.presentation.dto.response.AdminProjectSubmissionSummaryResponse;
import org.channel.ensharponlinejudge.submission.presentation.dto.response.AdminSubmissionDetailResponse;
import org.channel.ensharponlinejudge.submission.presentation.dto.response.AdminUserProjectSubmissionResponse;
import org.channel.ensharponlinejudge.submission.repository.SubmissionRepository;
import org.channel.ensharponlinejudge.submission.repository.SubmissionResultDetailRepository;
import org.channel.ensharponlinejudge.user.domain.User;
import org.channel.ensharponlinejudge.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminSubmissionService {

  private final SubmissionRepository submissionRepository;
  private final UserRepository userRepository;
  private final ProjectRepository projectRepository;
  private final SubmissionResultDetailRepository submissionResultDetailRepository;

  public List<AdminProjectSubmissionSummaryResponse> getProjectSubmissionSummary(UUID projectId) {
    Project project =
        projectRepository
            .findById(projectId)
            .orElseThrow(() -> new BusinessException(ProjectErrorCode.ERR_PROJECT_NOT_FOUND));

    List<User> students = userRepository.findByGeneration(project.getGeneration());
    List<Submission> submissions = submissionRepository.findByProjectId(projectId);

    Map<UUID, Submission> latestSubmissions =
        submissions.stream()
            .collect(
                Collectors.toMap(
                    Submission::getUserId,
                    s -> s,
                    (existing, replacement) ->
                        existing.getSubmittedAt().isAfter(replacement.getSubmittedAt())
                            ? existing
                            : replacement));

    return students.stream()
        .map(
            user -> {
              Submission latest = latestSubmissions.get(user.getId());
              return AdminProjectSubmissionSummaryResponse.builder()
                  .userId(user.getId())
                  .submissionId(latest != null ? latest.getId() : null)
                  .name(user.getName())
                  .githubId(user.getGithubId())
                  .status(latest != null ? latest.getStatus() : null)
                  .score(latest != null ? latest.getScore() : null)
                  .lastSubmittedAt(latest != null ? latest.getSubmittedAt() : null)
                  .build();
            })
        .collect(Collectors.toList());
  }

  public List<AdminGlobalSubmissionResponse> getGlobalSubmissions() {
    List<Submission> submissions = submissionRepository.findAllByOrderBySubmittedAtDesc();

    Set<UUID> userIds = submissions.stream().map(Submission::getUserId).collect(Collectors.toSet());
    Set<UUID> projectIds =
        submissions.stream().map(Submission::getProjectId).collect(Collectors.toSet());

    Map<UUID, User> userMap =
        userRepository.findAllById(userIds).stream().collect(Collectors.toMap(User::getId, u -> u));
    Map<UUID, Project> projectMap =
        projectRepository.findAllById(projectIds).stream()
            .collect(Collectors.toMap(Project::getId, p -> p));

    return submissions.stream()
        .map(
            s -> {
              User user = userMap.get(s.getUserId());
              Project project = projectMap.get(s.getProjectId());

              return AdminGlobalSubmissionResponse.builder()
                  .submissionId(s.getId())
                  .projectId(s.getProjectId())
                  .submittedAt(s.getSubmittedAt())
                  .githubId(user != null ? user.getGithubId() : "Unknown")
                  .name(user != null ? user.getName() : "Unknown")
                  .problemTitle(project != null ? project.getTitle() : "Unknown")
                  .status(s.getStatus())
                  .score(s.getScore())
                  .memoryUsage(s.getMemoryUsage())
                  .timeUsage(s.getTimeUsage())
                  .build();
            })
        .collect(Collectors.toList());
  }

  public AdminSubmissionDetailResponse getAdminSubmissionDetail(UUID submissionId) {
    Submission submission =
        submissionRepository
            .findById(submissionId)
            .orElseThrow(() -> new BusinessException(SubmissionErrorCode.SUBMISSION_NOT_FOUND));

    User user =
        userRepository
            .findById(submission.getUserId())
            .orElseThrow(() -> new BusinessException(SubmissionErrorCode.USER_NOT_FOUND));

    List<SubmissionResultDetail> details =
        submissionResultDetailRepository.findBySubmissionId(submissionId);

    List<AdminSubmissionDetailResponse.TestDetailResponse> testDetails =
        details.stream()
            .map(
                d ->
                    AdminSubmissionDetailResponse.TestDetailResponse.builder()
                        .methodName(d.getMethodName())
                        .status(d.getStatus())
                        .durationMs(d.getDurationMs())
                        .message(d.getMessage())
                        .isHidden(d.isHidden())
                        .score(d.getScore())
                        .build())
            .toList();

    return AdminSubmissionDetailResponse.builder()
        .submissionId(submission.getId())
        .userName(user.getName())
        .githubId(user.getGithubId())
        .repoUrl(submission.getRepoUrl())
        .status(submission.getStatus())
        .score(submission.getScore())
        .memoryUsage(submission.getMemoryUsage())
        .timeExecution(submission.getTimeUsage())
        .submittedAt(submission.getSubmittedAt())
        .sourceCode("") // Still need to find where source code is stored
        .testDetails(testDetails)
        .build();
  }

  public List<AdminUserProjectSubmissionResponse> getUserProjectSubmissions(
      UUID projectId, UUID userId) {
    List<Submission> submissions =
        submissionRepository.findByUserIdAndProjectIdOrderBySubmittedAtDesc(userId, projectId);

    return submissions.stream()
        .map(
            s ->
                AdminUserProjectSubmissionResponse.builder()
                    .submissionId(s.getId())
                    .status(s.getStatus())
                    .score(s.getScore())
                    .memoryUsage(s.getMemoryUsage())
                    .timeExecution(s.getTimeUsage())
                    .language("Java")
                    .submittedAt(s.getSubmittedAt())
                    .build())
        .collect(Collectors.toList());
  }
}
