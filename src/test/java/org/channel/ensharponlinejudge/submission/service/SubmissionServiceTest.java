package org.channel.ensharponlinejudge.submission.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

import java.util.Optional;
import java.util.UUID;

import org.channel.ensharponlinejudge.exception.BusinessException;
import org.channel.ensharponlinejudge.exception.enums.SubmissionErrorCode;
import org.channel.ensharponlinejudge.project.domain.Project;
import org.channel.ensharponlinejudge.project.repository.ProjectRepository;
import org.channel.ensharponlinejudge.submission.domain.SubmissionStatus;
import org.channel.ensharponlinejudge.submission.repository.SubmissionRepository;
import org.channel.ensharponlinejudge.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class SubmissionServiceTest {

  @Mock SubmissionRepository submissionRepository;
  @Mock ProjectRepository projectRepository;
  @Mock UserRepository userRepository;

  @InjectMocks SubmissionService submissionService;

  @Test
  void 프로젝트_없으면_404() {

    UUID userId = UUID.randomUUID();
    UUID projectId = UUID.randomUUID();
    String repoUrl = "http://gitbub,com/vvineey/example";

    // given(projectRepository.existsById(projectId)).willReturn(false);
    given(projectRepository.findById(projectId)).willReturn(Optional.empty());

    BusinessException ex =
        assertThrows(
            BusinessException.class, () -> submissionService.submit(userId, projectId, repoUrl));

    assertThat(ex.getErrorCode()).isEqualTo(SubmissionErrorCode.PROJECT_NOT_FOUND);

    then(submissionRepository).shouldHaveNoInteractions();
  }

  @Test
  void 채점중인_제출이_있으면_409() {

    UUID userId = UUID.randomUUID();
    UUID projectId = UUID.randomUUID();
    String repoUrl = "http://gitbub,com/vvineey/example";

    // 프로젝트 존재
    given(projectRepository.findById(projectId)).willReturn(Optional.of(mock(Project.class)));

    // 이미 채점 중인 제출이 있다!!!
    given(
            submissionRepository.existsByUserIdAndProjectIdAndStatusIn(
                eq(userId),
                eq(projectId),
                argThat(
                    statuses ->
                        statuses.contains(SubmissionStatus.QUEUED)
                            && statuses.contains(SubmissionStatus.PROCESSING))))
        .willReturn(true);

    BusinessException ex =
        assertThrows(
            BusinessException.class, () -> submissionService.submit(userId, projectId, repoUrl));

    assertThat(ex.getErrorCode()).isEqualTo(SubmissionErrorCode.SUBMISSION_IN_PROGRESS);

    then(submissionRepository).shouldHaveNoMoreInteractions();
  }
}
