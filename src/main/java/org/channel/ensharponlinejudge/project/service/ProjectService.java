package org.channel.ensharponlinejudge.project.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.channel.ensharponlinejudge.exception.BusinessException;
import org.channel.ensharponlinejudge.exception.enums.ProjectErrorCode;
import org.channel.ensharponlinejudge.project.domain.Project;
import org.channel.ensharponlinejudge.project.domain.ProjectTestCase;
import org.channel.ensharponlinejudge.project.presentation.dto.request.ProjectCreateRequest;
import org.channel.ensharponlinejudge.project.presentation.dto.request.ProjectUpdateRequest;
import org.channel.ensharponlinejudge.project.presentation.dto.request.ScorePolicyDto;
import org.channel.ensharponlinejudge.project.presentation.dto.request.TestCaseDto;
import org.channel.ensharponlinejudge.project.presentation.dto.response.AdminProjectDetailResponse;
import org.channel.ensharponlinejudge.project.presentation.dto.response.AdminProjectListResponse;
import org.channel.ensharponlinejudge.project.presentation.dto.response.MenteeProjectDetailResponse;
import org.channel.ensharponlinejudge.project.presentation.dto.response.MenteeProjectListResponse;
import org.channel.ensharponlinejudge.project.repository.ProjectRepository;
import org.channel.ensharponlinejudge.project.repository.ProjectTestCaseRepository;
import org.channel.ensharponlinejudge.user.domain.Role;
import org.channel.ensharponlinejudge.user.domain.User;
import org.channel.ensharponlinejudge.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProjectService {
  private final ProjectRepository projectRepository;
  private final ProjectTestCaseRepository projectTestCaseRepository;
  private final UserRepository userRepository;

  @Transactional
  public UUID createProject(UUID userId, ProjectCreateRequest request) {
    User user = userRepository
        .findById(userId)
        .orElseThrow(() -> new BusinessException(ProjectErrorCode.ERR_FORBIDDEN));

    if (user.getRole() != Role.MENTOR) {
      throw new BusinessException(ProjectErrorCode.ERR_FORBIDDEN);
    }

    Project project = Project.initialize(
        request.generation(),
        request.type(),
        request.title(),
        request.description(),
        request.startDate(),
        request.dueDate(),
        request.skeletonUrl(),
        request.testCodeUrl(),
        request.scorePolicy().timeLimit(),
        request.scorePolicy().memoryLimit());

    Project savedProject = projectRepository.save(project);

    if (request.scorePolicy().cases() != null) {
      List<ProjectTestCase> testCases = request.scorePolicy().cases().stream()
          .map(
              testCaseDto -> ProjectTestCase.initialize(
                  savedProject,
                  testCaseDto.name(),
                  testCaseDto.score(),
                  testCaseDto.isHidden()))
          .collect(Collectors.toList());

      projectTestCaseRepository.saveAll(testCases);
    }

    return savedProject.getId();
  }

  @Transactional
  public void updateProject(UUID userId, UUID projectId, ProjectUpdateRequest request) {
    User user = userRepository
        .findById(userId)
        .orElseThrow(() -> new BusinessException(ProjectErrorCode.ERR_FORBIDDEN));

    if (user.getRole() != Role.MENTOR) {
      throw new BusinessException(ProjectErrorCode.ERR_FORBIDDEN);
    }

    Project project = projectRepository
        .findById(projectId)
        .orElseThrow(() -> new BusinessException(ProjectErrorCode.ERR_PROJECT_NOT_FOUND));

    project.update(
        request.type(),
        request.title(),
        request.description(),
        request.startDate(),
        request.dueDate(),
        request.skeletonUrl(),
        request.testCodeUrl(),
        request.scorePolicy().timeLimit(),
        request.scorePolicy().memoryLimit());

    // Delete existing cases and save new ones
    projectTestCaseRepository.deleteByProjectId(projectId);

    if (request.scorePolicy().cases() != null) {
      List<ProjectTestCase> testCases = request.scorePolicy().cases().stream()
          .map(
              testCaseDto -> ProjectTestCase.initialize(
                  project, testCaseDto.name(), testCaseDto.score(), testCaseDto.isHidden()))
          .collect(Collectors.toList());
      projectTestCaseRepository.saveAll(testCases);
    }
  }

  @Transactional
  public void deleteProject(UUID userId, UUID projectId) {
    User user = userRepository
        .findById(userId)
        .orElseThrow(() -> new BusinessException(ProjectErrorCode.ERR_FORBIDDEN));

    if (user.getRole() != Role.MENTOR) {
      throw new BusinessException(ProjectErrorCode.ERR_FORBIDDEN);
    }

    Project project = projectRepository
        .findById(projectId)
        .orElseThrow(() -> new BusinessException(ProjectErrorCode.ERR_PROJECT_NOT_FOUND));

    projectTestCaseRepository.deleteByProjectId(projectId);
    projectRepository.delete(project);
  }

  @Transactional(readOnly = true)
  public List<AdminProjectListResponse> getAdminProjects(UUID userId) {
    User user = userRepository
        .findById(userId)
        .orElseThrow(() -> new BusinessException(ProjectErrorCode.ERR_FORBIDDEN));

    if (user.getRole() != Role.MENTOR) {
      throw new BusinessException(ProjectErrorCode.ERR_FORBIDDEN);
    }

    return projectRepository.findAll().stream()
        .map(
            project -> AdminProjectListResponse.builder()
                .id(project.getId())
                .title(project.getTitle())
                .type(project.getType())
                .generation(project.getGeneration())
                .startDate(project.getStartDate())
                .dueDate(project.getDueDate())
                .build())
        .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public AdminProjectDetailResponse getAdminProjectDetail(UUID userId, UUID projectId) {
    User user = userRepository
        .findById(userId)
        .orElseThrow(() -> new BusinessException(ProjectErrorCode.ERR_FORBIDDEN));

    if (user.getRole() != Role.MENTOR) {
      throw new BusinessException(ProjectErrorCode.ERR_FORBIDDEN);
    }

    Project project = projectRepository
        .findById(projectId)
        .orElseThrow(() -> new BusinessException(ProjectErrorCode.ERR_PROJECT_NOT_FOUND));

    List<ProjectTestCase> testCases = projectTestCaseRepository.findByProjectId(projectId);

    List<TestCaseDto> caseDtos = testCases.stream()
        .map(
            tc -> TestCaseDto.builder()
                .id(tc.getId() != null ? tc.getId().toString() : null)
                .name(tc.getName())
                .score(tc.getScore())
                .isHidden(tc.isHidden())
                .build())
        .collect(Collectors.toList());

    ScorePolicyDto scorePolicy = ScorePolicyDto.builder()
        .timeLimit(project.getTimeLimit())
        .memoryLimit(project.getMemoryLimit())
        .cases(caseDtos)
        .build();

    return AdminProjectDetailResponse.builder()
        .id(project.getId())
        .title(project.getTitle())
        .type(project.getType())
        .generation(project.getGeneration())
        .description(project.getDescription())
        .startDate(project.getStartDate())
        .dueDate(project.getDueDate())
        .skeletonUrl(project.getSkeletonUrl())
        .testCodeUrl(project.getTestCodeUrl())
        .scorePolicy(scorePolicy)
        .build();
  }

  @Transactional(readOnly = true)
  public List<MenteeProjectListResponse> getMenteeProjects(UUID userId) {
    User user = userRepository
        .findById(userId)
        .orElseThrow(() -> new BusinessException(ProjectErrorCode.ERR_FORBIDDEN));

    return projectRepository.findAll().stream()
        .filter(project -> project.getGeneration() == user.getGeneration())
        .map(
            project -> MenteeProjectListResponse.builder()
                .id(project.getId())
                .title(project.getTitle())
                .type(project.getType())
                .startDate(project.getStartDate())
                .dueDate(project.getDueDate())
                .build())
        .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public MenteeProjectDetailResponse getMenteeProjectDetail(UUID userId, UUID projectId) {
    User user = userRepository
        .findById(userId)
        .orElseThrow(() -> new BusinessException(ProjectErrorCode.ERR_FORBIDDEN));

    Project project = projectRepository
        .findById(projectId)
        .orElseThrow(() -> new BusinessException(ProjectErrorCode.ERR_PROJECT_NOT_FOUND));

    if (project.getGeneration() != user.getGeneration()) {
      throw new BusinessException(ProjectErrorCode.ERR_FORBIDDEN);
    }

    List<ProjectTestCase> testCases = projectTestCaseRepository.findByProjectId(projectId);

    List<TestCaseDto> caseDtos = testCases.stream()
        .filter(tc -> !tc.isHidden()) // Hide hidden test cases
        .map(
            tc -> TestCaseDto.builder()
                .id(tc.getId() != null ? tc.getId().toString() : null)
                .name(tc.getName())
                .score(tc.getScore())
                .isHidden(tc.isHidden())
                .build())
        .collect(Collectors.toList());

    ScorePolicyDto scorePolicy = ScorePolicyDto.builder()
        .timeLimit(project.getTimeLimit())
        .memoryLimit(project.getMemoryLimit())
        .cases(caseDtos)
        .build();

    return MenteeProjectDetailResponse.builder()
        .id(project.getId())
        .title(project.getTitle())
        .type(project.getType())
        .description(project.getDescription())
        .startDate(project.getStartDate())
        .dueDate(project.getDueDate())
        .skeletonUrl(project.getSkeletonUrl())
        // Mentee response explicitly excludes testCodeUrl
        .scorePolicy(scorePolicy)
        .build();
  }
}
