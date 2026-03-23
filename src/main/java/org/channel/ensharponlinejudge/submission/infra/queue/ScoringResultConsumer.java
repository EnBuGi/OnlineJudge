package org.channel.ensharponlinejudge.submission.infra.queue;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.channel.ensharponlinejudge.project.domain.ProjectTestCase;
import org.channel.ensharponlinejudge.project.repository.ProjectTestCaseRepository;
import org.channel.ensharponlinejudge.submission.domain.Submission;
import org.channel.ensharponlinejudge.submission.domain.SubmissionResultDetail;
import org.channel.ensharponlinejudge.submission.domain.SubmissionStatus;
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

      // Build individual test case results and calculate total score from API server's source of truth
      int calculatedScore = 0;
      List<SubmissionResultDetail> details = null;

      if (result.getDetails() != null) {
        details =
            result.getDetails().stream()
                .map(
                    d -> {
                      ProjectTestCase tc = testCaseMap.get(d.getMethodName());
                      boolean hidden = tc != null && tc.isHidden();
                      Integer testCaseScore = tc != null ? tc.getScore() : 0;
                      boolean passed = "PASSED".equals(d.getStatus());

                      return SubmissionResultDetail.builder()
                          .submissionId(submissionId)
                          .methodName(d.getMethodName())
                          .status(TestStatus.valueOf(d.getStatus()))
                          .durationMs((int) d.getDurationMs())
                          .message(d.getMessage())
                          .isHidden(hidden)
                          .score(passed ? testCaseScore : 0)
                          .build();
                    })
                .toList();

        // Sum up the scores of PASSED test cases
        calculatedScore =
            details.stream()
                .filter(d -> d.getStatus() == TestStatus.PASSED)
                .mapToInt(SubmissionResultDetail::getScore)
                .sum();
      }

      String overallStatus = result.getOverallStatus();
      if ("ACCEPTED".equals(overallStatus) || overallStatus == null) {
        submission.markCompleted(calculatedScore, result.getTotalTests(), result.getPassedTests());
      } else if ("EXECUTION_ERROR".equals(overallStatus)) {
        submission.markSystemError();
      } else {
        try {
          SubmissionStatus status = SubmissionStatus.valueOf(overallStatus);
          submission.markFailed(
              status, calculatedScore, result.getTotalTests(), result.getPassedTests());
        } catch (IllegalArgumentException e) {
          log.warn("Unknown overallStatus: {}, falling back to COMPLETED", overallStatus);
          submission.markCompleted(
              calculatedScore, result.getTotalTests(), result.getPassedTests());
        }
      }

      if (details != null) {
        submissionResultDetailRepository.saveAll(details);
      }

      log.info(
          "Scoring result processed: submissionId={}, score={}, passed={}/{}",
          submissionId,
          calculatedScore,
          result.getPassedTests(),
          result.getTotalTests());

    } catch (Exception e) {
      log.error("Failed to process scoring result message", e);
    }
  }
}
