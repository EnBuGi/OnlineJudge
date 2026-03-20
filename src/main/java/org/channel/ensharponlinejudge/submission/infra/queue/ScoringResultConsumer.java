package org.channel.ensharponlinejudge.submission.infra.queue;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.channel.ensharponlinejudge.project.domain.ProjectTestCase;
import org.channel.ensharponlinejudge.project.repository.ProjectTestCaseRepository;
import org.channel.ensharponlinejudge.submission.domain.Submission;
import org.channel.ensharponlinejudge.submission.domain.SubmissionResultDetail;
import org.channel.ensharponlinejudge.submission.domain.TestStatus;
import org.channel.ensharponlinejudge.submission.infra.queue.dto.ScoringResultMessage;
import org.channel.ensharponlinejudge.submission.repository.SubmissionRepository;
import org.channel.ensharponlinejudge.submission.repository.SubmissionResultDetailRepository;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScoringResultConsumer implements MessageListener {

  private final SubmissionRepository submissionRepository;
  private final SubmissionResultDetailRepository submissionResultDetailRepository;
  private final ProjectTestCaseRepository projectTestCaseRepository;
  private final ObjectMapper objectMapper;

  @Override
  @Transactional
  public void onMessage(Message message, byte[] pattern) {
    try {
      String json = new String(message.getBody());
      ScoringResultMessage result = objectMapper.readValue(json, ScoringResultMessage.class);

      UUID submissionId = UUID.fromString(result.getSubmissionId());
      Submission submission = submissionRepository.findById(submissionId).orElse(null);

      if (submission == null) {
        log.warn("Submission not found for result: {}", submissionId);
        return;
      }

      // Build a map of test case name -> ProjectTestCase for isHidden/score lookup
      List<ProjectTestCase> testCases =
          projectTestCaseRepository.findByProjectId(submission.getProjectId());
      Map<String, ProjectTestCase> testCaseMap =
          testCases.stream()
              .collect(Collectors.toMap(ProjectTestCase::getName, tc -> tc, (a, b) -> a));

      //  score: (passedTests / totalTests) * 100
      int score = 0;
      if (result.getTotalTests() > 0) {
        score = (int) Math.round((double) result.getPassedTests() / result.getTotalTests() * 100);
      }

      submission.markCompleted(score, result.getTotalTests(), result.getPassedTests());

      // Save individual test case results
      if (result.getDetails() != null) {
        List<SubmissionResultDetail> details =
            result.getDetails().stream()
                .map(
                    d -> {
                      ProjectTestCase tc = testCaseMap.get(d.getMethodName());
                      boolean hidden = tc != null && tc.isHidden();
                      Integer testScore = tc != null ? tc.getScore() : null;

                      return SubmissionResultDetail.builder()
                          .submissionId(submissionId)
                          .methodName(d.getMethodName())
                          .status(TestStatus.valueOf(d.getStatus()))
                          .durationMs((int) d.getDurationMs())
                          .message(d.getMessage())
                          .isHidden(hidden)
                          .score(testScore != null ? testScore : 0)
                          .build();
                    })
                .toList();

        submissionResultDetailRepository.saveAll(details);
      }

      log.info(
          "Scoring result processed: submissionId={}, score={}, passed={}/{}",
          submissionId,
          score,
          result.getPassedTests(),
          result.getTotalTests());

    } catch (Exception e) {
      log.error("Failed to process scoring result message", e);
    }
  }
}
