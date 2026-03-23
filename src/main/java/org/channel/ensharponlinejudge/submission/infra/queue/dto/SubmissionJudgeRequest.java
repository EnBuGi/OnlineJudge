package org.channel.ensharponlinejudge.submission.infra.queue.dto;

import java.util.List;
import java.util.UUID;

import org.channel.ensharponlinejudge.project.presentation.dto.request.TestCaseDto;

public record SubmissionJudgeRequest(
    UUID submissionId,
    UUID userId,
    UUID projectId,
    String repoUrl,
    String testCodeUrl,
    Integer timeLimit,
    Integer memoryLimit,
    String projectType,
    List<TestCaseDto> testCases,
    String githubAccessToken) {
  @Override
  public String toString() {
    return "SubmissionJudgeRequest["
        + "submissionId="
        + submissionId
        + ", userId="
        + userId
        + ", projectId="
        + projectId
        + ", repoUrl="
        + repoUrl
        + ", testCodeUrl="
        + testCodeUrl
        + ", timeLimit="
        + timeLimit
        + ", memoryLimit="
        + memoryLimit
        + ", projectType="
        + projectType
        + ", testCases="
        + testCases
        + ", githubAccessToken=[PROTECTED]"
        + "]";
  }
}
