package org.channel.ensharponlinejudge.project.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.channel.ensharponlinejudge.exception.BusinessException;
import org.channel.ensharponlinejudge.exception.enums.ProjectErrorCode;
import org.channel.ensharponlinejudge.project.domain.Project;
import org.channel.ensharponlinejudge.project.domain.ProjectTestCase;
import org.channel.ensharponlinejudge.project.infra.storage.ObjectStorageService;
import org.channel.ensharponlinejudge.project.presentation.dto.request.ProjectCreateRequest;
import org.channel.ensharponlinejudge.project.presentation.dto.request.ProjectUpdateRequest;
import org.channel.ensharponlinejudge.project.presentation.dto.request.ScorePolicyDto;
import org.channel.ensharponlinejudge.project.presentation.dto.request.TestCaseDto;
import org.channel.ensharponlinejudge.project.presentation.dto.response.AdminProjectDetailResponse;
import org.channel.ensharponlinejudge.project.presentation.dto.response.AdminProjectListResponse;
import org.channel.ensharponlinejudge.project.presentation.dto.response.MenteeProjectDetailResponse;
import org.channel.ensharponlinejudge.project.presentation.dto.response.MenteeProjectListResponse;
import org.channel.ensharponlinejudge.project.presentation.dto.response.TestCodeParseResponse;
import org.channel.ensharponlinejudge.project.presentation.dto.response.TestCodeUploadResponse;
import org.channel.ensharponlinejudge.project.repository.ProjectRepository;
import org.channel.ensharponlinejudge.project.repository.ProjectTestCaseRepository;
import org.channel.ensharponlinejudge.user.domain.Role;
import org.channel.ensharponlinejudge.user.domain.User;
import org.channel.ensharponlinejudge.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectService {
  private final ProjectRepository projectRepository;
  private final ProjectTestCaseRepository projectTestCaseRepository;
  private final UserRepository userRepository;
  private final ObjectStorageService objectStorageService;

  @Transactional
  public UUID createProject(UUID userId, ProjectCreateRequest request) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new BusinessException(ProjectErrorCode.ERR_FORBIDDEN));

    if (user.getRole() != Role.MENTOR) {
      throw new BusinessException(ProjectErrorCode.ERR_FORBIDDEN);
    }

    Project project =
        Project.initialize(
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
      List<ProjectTestCase> testCases =
          request.scorePolicy().cases().stream()
              .map(
                  testCaseDto ->
                      ProjectTestCase.initialize(
                          savedProject,
                          testCaseDto.name(),
                          testCaseDto.score(),
                          testCaseDto.isHidden()))
              .collect(Collectors.toList());

      projectTestCaseRepository.saveAll(testCases);
    }

    handleTestCodeKey(savedProject, request.testCodeKey());

    return savedProject.getId();
  }

  @Transactional
  public void updateProject(UUID userId, UUID projectId, ProjectUpdateRequest request) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new BusinessException(ProjectErrorCode.ERR_FORBIDDEN));

    if (user.getRole() != Role.MENTOR) {
      throw new BusinessException(ProjectErrorCode.ERR_FORBIDDEN);
    }

    Project project =
        projectRepository
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
      List<ProjectTestCase> testCases =
          request.scorePolicy().cases().stream()
              .map(
                  testCaseDto ->
                      ProjectTestCase.initialize(
                          project, testCaseDto.name(), testCaseDto.score(), testCaseDto.isHidden()))
              .collect(Collectors.toList());
      projectTestCaseRepository.saveAll(testCases);
    }

    handleTestCodeKey(project, request.testCodeKey());
  }

  @Transactional
  public void deleteProject(UUID userId, UUID projectId) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new BusinessException(ProjectErrorCode.ERR_FORBIDDEN));

    if (user.getRole() != Role.MENTOR) {
      throw new BusinessException(ProjectErrorCode.ERR_FORBIDDEN);
    }

    Project project =
        projectRepository
            .findById(projectId)
            .orElseThrow(() -> new BusinessException(ProjectErrorCode.ERR_PROJECT_NOT_FOUND));

    projectTestCaseRepository.deleteByProjectId(projectId);
    projectRepository.delete(project);
  }

  @Transactional(readOnly = true)
  public List<AdminProjectListResponse> getAdminProjects(UUID userId) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new BusinessException(ProjectErrorCode.ERR_FORBIDDEN));

    if (user.getRole() != Role.MENTOR) {
      throw new BusinessException(ProjectErrorCode.ERR_FORBIDDEN);
    }

    return projectRepository.findAll().stream()
        .map(
            project ->
                AdminProjectListResponse.builder()
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
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new BusinessException(ProjectErrorCode.ERR_FORBIDDEN));

    if (user.getRole() != Role.MENTOR) {
      throw new BusinessException(ProjectErrorCode.ERR_FORBIDDEN);
    }

    Project project =
        projectRepository
            .findById(projectId)
            .orElseThrow(() -> new BusinessException(ProjectErrorCode.ERR_PROJECT_NOT_FOUND));

    List<ProjectTestCase> testCases = projectTestCaseRepository.findByProjectId(projectId);

    List<TestCaseDto> caseDtos =
        testCases.stream()
            .map(
                tc ->
                    TestCaseDto.builder()
                        .id(tc.getId() != null ? tc.getId().toString() : null)
                        .name(tc.getName())
                        .score(tc.getScore())
                        .isHidden(tc.isHidden())
                        .build())
            .collect(Collectors.toList());

    ScorePolicyDto scorePolicy =
        ScorePolicyDto.builder()
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
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new BusinessException(ProjectErrorCode.ERR_FORBIDDEN));

    return projectRepository.findAll().stream()
        .filter(project -> project.getGeneration() >= user.getGeneration())
        .map(
            project ->
                MenteeProjectListResponse.builder()
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
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new BusinessException(ProjectErrorCode.ERR_FORBIDDEN));

    Project project =
        projectRepository
            .findById(projectId)
            .orElseThrow(() -> new BusinessException(ProjectErrorCode.ERR_PROJECT_NOT_FOUND));

    if (project.getGeneration() < user.getGeneration()) {
      throw new BusinessException(ProjectErrorCode.ERR_FORBIDDEN);
    }

    List<ProjectTestCase> testCases = projectTestCaseRepository.findByProjectId(projectId);

    List<TestCaseDto> caseDtos =
        testCases.stream()
            .map(
                tc ->
                    TestCaseDto.builder()
                        .id(tc.getId() != null ? tc.getId().toString() : null)
                        .name(tc.isHidden() ? "히든 테스트 케이스" : tc.getName())
                        .score(tc.getScore())
                        .isHidden(tc.isHidden())
                        .build())
            .collect(Collectors.toList());

    ScorePolicyDto scorePolicy =
        ScorePolicyDto.builder()
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

  public TestCodeParseResponse parseTestCode(UUID userId, MultipartFile file) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new BusinessException(ProjectErrorCode.ERR_FORBIDDEN));

    if (user.getRole() != Role.MENTOR) {
      throw new BusinessException(ProjectErrorCode.ERR_FORBIDDEN);
    }

    List<String> methodNames = new ArrayList<>();

    try (ZipInputStream zis = new ZipInputStream(file.getInputStream())) {
      ZipEntry entry;
      while ((entry = zis.getNextEntry()) != null) {
        if (!entry.isDirectory() && entry.getName().endsWith(".java")) {
          // Read content for parsing
          StringBuilder content = new StringBuilder();
          byte[] buffer = new byte[8192];
          int length;
          while ((length = zis.read(buffer)) != -1) {
            content.append(new String(buffer, 0, length));
          }

          // JUnit @Test annotation regex
          Pattern pattern = Pattern.compile("@Test\\s+(?:public\\s+)?void\\s+(\\w+)\\s*\\(");
          Matcher matcher = pattern.matcher(content.toString());

          while (matcher.find()) {
            methodNames.add(matcher.group(1));
          }
        }
      }
    } catch (IOException e) {
      throw new BusinessException(ProjectErrorCode.ERR_FILE_PARSE_FAILED);
    }

    // OCI 임시 경로에 업로드
    String tempKey = objectStorageService.uploadTempTestCode(file);

    return TestCodeParseResponse.builder().methodNames(methodNames).testCodeKey(tempKey).build();
  }

  /**
   * 테스트 코드 .zip 파일을 OCI 오브젝트 스토리지에 업로드하고, 생성된 PAR URL을 프로젝트에 저장합니다. 업로드 전 .zip 파일 내 src/test 디렉토리
   * 존재 여부를 검증합니다.
   */
  @Transactional
  public TestCodeUploadResponse uploadTestCode(UUID userId, UUID projectId, MultipartFile file) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new BusinessException(ProjectErrorCode.ERR_FORBIDDEN));

    if (user.getRole() != Role.MENTOR) {
      throw new BusinessException(ProjectErrorCode.ERR_FORBIDDEN);
    }

    Project project =
        projectRepository
            .findById(projectId)
            .orElseThrow(() -> new BusinessException(ProjectErrorCode.ERR_PROJECT_NOT_FOUND));

    // OCI에 업로드 (내부에서 .zip 및 src/test 디렉토리 검증 수행)
    String objectKey = "projects/" + projectId.toString() + "/test-code.zip";
    objectStorageService.uploadTestCode(file, objectKey);

    // PAR GET URL 생성
    String testCodeUrl = objectStorageService.generateGetUrl(objectKey);

    // 프로젝트에 testCodeUrl 저장
    project.updateTestCodeUrl(testCodeUrl);

    return TestCodeUploadResponse.builder().testCodeUrl(testCodeUrl).build();
  }

  private void handleTestCodeKey(Project project, String testCodeKey) {
    if (testCodeKey != null && !testCodeKey.isBlank()) {
      log.info(
          "[ProjectService] Handling testCodeKey for project {}: tempKey={}",
          project.getId(),
          testCodeKey);
      String permanentKey = "projects/" + project.getId().toString() + "/test-code.zip";

      try {
        objectStorageService.moveTempToPermanent(testCodeKey, permanentKey);
        log.info("[ProjectService] File moved successfully for project {}", project.getId());

        String testCodeUrl = objectStorageService.generateGetUrl(permanentKey);
        log.info(
            "[ProjectService] Generated testCodeUrl for project {}: {}",
            project.getId(),
            testCodeUrl);

        project.updateTestCodeUrl(testCodeUrl);
      } catch (Exception e) {
        log.error(
            "[ProjectService] Failed to handle testCodeKey for project {}: error={}",
            project.getId(),
            e.getMessage(),
            e);
        throw e;
      }
    } else {
      log.debug("[ProjectService] No testCodeKey provided for project {}", project.getId());
    }
  }
}
