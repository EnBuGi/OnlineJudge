package org.channel.ensharponlinejudge.submission.presentation;

import java.util.List;
import java.util.UUID;

import org.channel.ensharponlinejudge.submission.presentation.dto.response.AdminGlobalSubmissionResponse;
import org.channel.ensharponlinejudge.submission.presentation.dto.response.AdminProjectSubmissionSummaryResponse;
import org.channel.ensharponlinejudge.submission.presentation.dto.response.AdminSubmissionDetailResponse;
import org.channel.ensharponlinejudge.submission.presentation.dto.response.AdminUserProjectSubmissionResponse;
import org.channel.ensharponlinejudge.submission.service.AdminSubmissionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('MENTOR')")
public class AdminSubmissionController {

  private final AdminSubmissionService adminSubmissionService;

  @GetMapping("/projects/{projectId}/submissions/summary")
  public ResponseEntity<List<AdminProjectSubmissionSummaryResponse>> getProjectSummary(
      @PathVariable("projectId") UUID projectId) {
    return ResponseEntity.ok(adminSubmissionService.getProjectSubmissionSummary(projectId));
  }

  @GetMapping("/submissions")
  public ResponseEntity<List<AdminGlobalSubmissionResponse>> getGlobalSubmissions() {
    return ResponseEntity.ok(adminSubmissionService.getGlobalSubmissions());
  }

  @GetMapping("/submissions/{submissionId}")
  public ResponseEntity<AdminSubmissionDetailResponse> getSubmissionDetail(
      @PathVariable("submissionId") UUID submissionId) {
    return ResponseEntity.ok(adminSubmissionService.getAdminSubmissionDetail(submissionId));
  }

  @GetMapping("/projects/{projectId}/users/{userId}/submissions")
  public ResponseEntity<List<AdminUserProjectSubmissionResponse>> getUserProjectSubmissions(
      @PathVariable("projectId") UUID projectId, @PathVariable("userId") UUID userId) {
    return ResponseEntity.ok(adminSubmissionService.getUserProjectSubmissions(projectId, userId));
  }
}
