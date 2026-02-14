package org.channel.ensharponlinejudge.submission.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import java.util.UUID;

import org.channel.ensharponlinejudge.exception.BusinessException;
import org.channel.ensharponlinejudge.exception.enums.SubmissionErrorCode;
import org.channel.ensharponlinejudge.project.repository.ProjectRepository;
import org.channel.ensharponlinejudge.submission.repository.SubmissionRepository;
import org.channel.ensharponlinejudge.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class SubmissionServiceTest {

  @Mock
  SubmissionRepository SubmissionRepository;
  @Mock ProjectRepository projectRepository;
  @Mock UserRepository userRepository;

  @InjectMocks
  SubmissionService projectSubmissionService;

  @Test
  void 프로젝트_없으면_404() {

    UUID userId = UUID.randomUUID();
    UUID projectId = UUID.randomUUID();
    String repoUrl = "http://gitbub,com/vvineey/example";

    given(projectRepository.existsById(projectId)).willReturn(false);

    BusinessException ex =
        assertThrows(
            BusinessException.class,
            () -> projectSubmissionService.submit(userId, projectId, repoUrl));

    assertThat(ex.getErrorCode()).isEqualTo(SubmissionErrorCode.PROJECT_NOT_FOUND);

    then(SubmissionRepository).shouldHaveNoInteractions();
  }
}
