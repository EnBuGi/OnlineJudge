package org.channel.ensharponlinejudge.submission.infra.queue.dto;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ScoringResultMessage {
  private String submissionId;
  private String overallStatus;
  private int totalTests;
  private int passedTests;
  private List<TestDetailMessage> details;

  @Getter
  @NoArgsConstructor
  public static class TestDetailMessage {
    private String methodName;
    private String status;
    private long durationMs;
    private String message;
  }
}
