package org.channel.ensharponlinejudge.submission.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.channel.ensharponlinejudge.exception.BusinessException;
import org.channel.ensharponlinejudge.exception.enums.SubmissionErrorCode;
import org.channel.ensharponlinejudge.project.domain.Project;
import org.channel.ensharponlinejudge.project.repository.ProjectRepository;
import org.channel.ensharponlinejudge.submission.domain.Submission;
import org.channel.ensharponlinejudge.submission.domain.SubmissionStatus;
import org.channel.ensharponlinejudge.submission.infra.queue.dto.SubmissionCreatedEvent;
import org.channel.ensharponlinejudge.submission.presentation.dto.response.SubmissionHistoryResponse;
import org.channel.ensharponlinejudge.submission.repository.SubmissionRepository;
import org.channel.ensharponlinejudge.user.domain.User;
import org.channel.ensharponlinejudge.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
public class SubmissionServiceTest {

  @Mock SubmissionRepository submissionRepository;
  @Mock ProjectRepository projectRepository;
  @Mock UserRepository userRepository;
  @Mock ApplicationEventPublisher applicationEventPublisher;

  @InjectMocks SubmissionService submissionService;

  @DisplayName("존재하지 않는 프로젝트 제출 404 예외 테스트")
  @Test
  void Given존재하지_않는_프로젝트인데When제출을_시도하면_Expoect404에러가_발생한다() {

    UUID userId = UUID.randomUUID();
    UUID projectId = UUID.randomUUID();
    String repoUrl = "http://gitbub,com/vvineey/example";

    // 프로젝트는 존재 안 함
    given(projectRepository.findById(projectId)).willReturn(Optional.empty());

    BusinessException ex =
        assertThrows(
            BusinessException.class, () -> submissionService.submit(userId, projectId, repoUrl));

    assertThat(ex.getErrorCode()).isEqualTo(SubmissionErrorCode.PROJECT_NOT_FOUND);

    then(submissionRepository).shouldHaveNoInteractions();
    then(applicationEventPublisher).shouldHaveNoInteractions();
  }

  @DisplayName("존재하지 않는 유저 제출 404 예외 테스트")
  @Test
  void Given존재하지_않는_유저인데When제출을_시도하면_Expoect404에러가_발생한다() {

    UUID userId = UUID.randomUUID();
    UUID projectId = UUID.randomUUID();
    String repoUrl = "http://gitbub,com/vvineey/example";

    // 프로젝트 존재
    givenProjectExists(projectId);

    // 유저는 존재 안 함
    given(userRepository.findById(userId)).willReturn(Optional.empty());

    BusinessException ex =
        assertThrows(
            BusinessException.class, () -> submissionService.submit(userId, projectId, repoUrl));

    assertThat(ex.getErrorCode()).isEqualTo(SubmissionErrorCode.USER_NOT_FOUND);

    then(submissionRepository).shouldHaveNoInteractions();
    then(applicationEventPublisher).shouldHaveNoInteractions();
  }

  @DisplayName("채점 중인 상테에서 제출 409 예외 테스트")
  @Test
  void Given채점중인_제출이_존재하는_상태인데When제출을_시도하면Expect409에러가_발생한다() {

    UUID userId = UUID.randomUUID();
    UUID projectId = UUID.randomUUID();
    String repoUrl = "http://gitbub,com/vvineey/example";

    // 프로젝트, 유저 존재
    givenProjectAndUserExist(projectId, userId);

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

    then(applicationEventPublisher).shouldHaveNoInteractions();
    then(submissionRepository).should(org.mockito.Mockito.never()).save(any());
  }

  @DisplayName("정상 제출 테스트")
  @Test
  void Given제출이_가능한_상태에서When제출을_시도하면Expect제출이_성공한다() {

    UUID userId = UUID.randomUUID();
    UUID projectId = UUID.randomUUID();
    UUID submissionId = UUID.randomUUID();
    String repoUrl = "http://gitbub,com/vvineey/example";

    // 프로젝트, 유저 존재
    givenProjectAndUserExist(projectId, userId);

    // 진행중 상태는 없음
    given(
            submissionRepository.existsByUserIdAndProjectIdAndStatusIn(
                eq(userId), eq(projectId), any()))
        .willReturn(false);

    // 제출된 submission
    Submission mockSubmission = mock(Submission.class);
    given(submissionRepository.save(any(Submission.class))).willReturn(mockSubmission);
    given(mockSubmission.getId()).willReturn(submissionId);

    submissionService.submit(userId, projectId, repoUrl);

    ArgumentCaptor<Submission> captor = ArgumentCaptor.forClass(Submission.class);

    then(submissionRepository).should().save(captor.capture());

    Submission submission = captor.getValue();

    assertThat(submission.getUserId()).isEqualTo(userId);
    assertThat(submission.getProjectId()).isEqualTo(projectId);
    assertThat(submission.getRepoUrl()).isEqualTo(repoUrl);

    then(submissionRepository).shouldHaveNoMoreInteractions();
  }

  @Test
  @DisplayName("정상 제출시 정상 이벤트 발행 테스트")
  void Given제출이_가능한_상태에서When제출을_시도하면Expect이벤트가_발행된다() {

    UUID userid = UUID.randomUUID();
    UUID projectId = UUID.randomUUID();
    String repoUrl = "http://gitbub,com/vvineey/example";
    UUID submisssionId = UUID.randomUUID();

    // 유저, 프로젝트 id
    given(projectRepository.findById(projectId)).willReturn(Optional.of(mock(Project.class)));
    given(userRepository.findById(userid)).willReturn(Optional.of(mock(User.class)));

    Submission mockSubmission = mock(Submission.class);

    given(submissionRepository.save(any())).willReturn(mockSubmission);
    given(mockSubmission.getId()).willReturn(submisssionId);

    submissionService.submit(userid, projectId, repoUrl);

    then(applicationEventPublisher)
        .should()
        .publishEvent(new SubmissionCreatedEvent(submisssionId));
  }

  @DisplayName("정상 채점 기록 조회 테스트")
  @Test
  void Given제출기록이있을때_When조회를_요청하면_Expect리스트가_반환된다() {
    UUID userId = UUID.randomUUID();
    UUID projectId = UUID.randomUUID();
    Pageable pageable = PageRequest.of(0, 10);

    givenProjectAndUserExist(projectId, userId);

    Submission mockSubmission = mock(Submission.class);
    given(mockSubmission.getId()).willReturn(UUID.randomUUID());
    given(mockSubmission.getRepoUrl()).willReturn("http://github.com/vvineey/example");
    given(mockSubmission.getStatus()).willReturn(SubmissionStatus.COMPLETED);
    given(mockSubmission.getScore()).willReturn(100);
    given(mockSubmission.getSubmittedAt()).willReturn(LocalDateTime.now());

    given(
            submissionRepository.findByUserIdAndProjectIdOrderBySubmittedAtDesc(
                userId, projectId, pageable))
        .willReturn(new PageImpl<>(List.of(mockSubmission)));

    Page<SubmissionHistoryResponse> result =
        submissionService.getSubmissionHistory(userId, projectId, pageable);

    assertThat(result.getTotalElements()).isEqualTo(1);
    assertThat(result.getContent().get(0).status()).isEqualTo(SubmissionStatus.COMPLETED);
    assertThat(result.getContent().get(0).score()).isEqualTo(100);
  }

  // 프로젝트 존재 기본 전제
  private void givenProjectExists(UUID projectId) {
    given(projectRepository.findById(projectId)).willReturn(Optional.of(mock(Project.class)));
  }

  // 유저 존재 기본 전제
  private void givenUserExists(UUID userId) {
    given(userRepository.findById(userId)).willReturn(Optional.of(mock(User.class)));
  }

  // 프로젝트, 유저 존재 기본 전제
  private void givenProjectAndUserExist(UUID projectId, UUID userId) {
    givenProjectExists(projectId);
    givenUserExists(userId);
  }
}
