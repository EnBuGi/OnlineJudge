package org.channel.ensharponlinejudge.project.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
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

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;

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
        request.generation(),
        request.type(),
        request.title(),
        request.description(),
        request.startDate(),
        request.dueDate(),
        request.skeletonUrl(),
        request.scorePolicy().timeLimit(),
        request.scorePolicy().memoryLimit());

    // 테스트 코드 키가 있으면 처리
    handleTestCodeKey(project, request.testCodeKey());

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

    String testCodeUrl = project.getTestCodeUrl();
    if (testCodeUrl != null && !testCodeUrl.isBlank() && !testCodeUrl.startsWith("http")) {
      // DB에 저장된 값이 URL이 아닌 객체 키인 경우 실시간 PAR 발급
      try {
        testCodeUrl = objectStorageService.generateGetUrl(testCodeUrl);
      } catch (Exception e) {
        log.error("Failed to generate PAR URL for project {}: {}", projectId, e.getMessage());
      }
    }

    return AdminProjectDetailResponse.builder()
        .id(project.getId())
        .title(project.getTitle())
        .type(project.getType())
        .generation(project.getGeneration())
        .description(project.getDescription())
        .startDate(project.getStartDate())
        .dueDate(project.getDueDate())
        .skeletonUrl(project.getSkeletonUrl())
        .testCodeUrl(testCodeUrl)
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
          try {
            // ZipInputStream이 중도에 닫히는 것을 방지하기 위해 데이터를 먼저 읽음
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buf = new byte[8192];
            int len;
            while ((len = zis.read(buf)) != -1) {
              baos.write(buf, 0, len);
            }

            if (baos.size() == 0) continue;

            // JavaParser를 사용하여 소스 코드 파싱
            CompilationUnit cu = StaticJavaParser.parse(baos.toString());

            // 모든 메서드 선언을 찾아서 어노테이션 확인
            cu.findAll(MethodDeclaration.class)
                .forEach(
                    method -> {
                      boolean isTest =
                          method.getAnnotations().stream()
                              .anyMatch(
                                  ann -> {
                                    String name = ann.getNameAsString();
                                    return name.equals("Test")
                                        || name.equals("ParameterizedTest")
                                        || name.equals("RepeatedTest")
                                        || name.endsWith(".Test")
                                        || name.endsWith(".ParameterizedTest")
                                        || name.endsWith(".RepeatedTest");
                                  });

                      if (isTest) {
                        methodNames.add(method.getNameAsString());
                      }
                    });
          } catch (Exception e) {
            // 개별 파일 파싱 실패 시 로그를 남기고 다음 파일로 진행
            log.warn(
                "Failed to parse java file in zip: {}. Error: {}", entry.getName(), e.getMessage());
          }
        }
      }
    } catch (IOException e) {
      log.error("IO error while parsing test code zip", e);
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

    // 프로젝트에 객체 키(objectKey) 저장 (이전에는 PAR URL을 저장했으나 만료 문제로 변경)
    project.updateTestCodeUrl(objectKey);

    // 반환용 PAR URL 생성 (프론트엔드 즉시 확인용)
    String testCodeUrl = objectStorageService.generateGetUrl(objectKey);

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

        // 프로젝트에 객체 키(permanentKey) 저장
        project.updateTestCodeUrl(permanentKey);
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
