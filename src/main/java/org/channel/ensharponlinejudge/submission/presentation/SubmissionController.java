package org.channel.ensharponlinejudge.submission.presentation;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;

import org.channel.ensharponlinejudge.submission.presentation.dto.request.SubmitRequest;
import org.channel.ensharponlinejudge.submission.presentation.dto.response.SubmissionDetailResponse;
import org.channel.ensharponlinejudge.submission.presentation.dto.response.SubmissionHistoryResponse;
import org.channel.ensharponlinejudge.submission.presentation.dto.response.SubmitResponse;
import org.channel.ensharponlinejudge.submission.service.SubmissionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/projects/{projectId}/submissions")
public class SubmissionController {

  private final SubmissionService submissionService;

  @PostMapping
  public ResponseEntity<SubmitResponse> submit(
      @PathVariable("projectId") UUID projectId,
      @AuthenticationPrincipal UserDetails userDetails,
      @Valid @RequestBody SubmitRequest submitRequest) {

    UUID userId = UUID.fromString(userDetails.getUsername());
    UUID submissionId = submissionService.submit(userId, projectId, submitRequest.repoUrl());

    return ResponseEntity.status(201).body(new SubmitResponse(submissionId));
  }

  @GetMapping
  public ResponseEntity<List<SubmissionHistoryResponse>> getSubmissionHistory(
      @PathVariable("projectId") UUID projectId, @AuthenticationPrincipal UserDetails userDetails) {

    UUID userId = UUID.fromString(userDetails.getUsername());
    List<SubmissionHistoryResponse> history =
        submissionService.getSubmissionHistory(userId, projectId);

    return ResponseEntity.ok(history);
  }

  @GetMapping("/{submissionId}")
  public ResponseEntity<SubmissionDetailResponse> getSubmissionDetail(
      @PathVariable("projectId") UUID projectId,
      @PathVariable("submissionId") UUID submissionId,
      @AuthenticationPrincipal UserDetails userDetails) {

    UUID userId = UUID.fromString(userDetails.getUsername());
    SubmissionDetailResponse detail = submissionService.getSubmissionDetail(userId, submissionId);

    return ResponseEntity.ok(detail);
  }

  @PostMapping("/{submissionId}/cancel")
  public ResponseEntity<Void> cancelSubmission(
      @PathVariable("projectId") UUID projectId,
      @PathVariable("submissionId") UUID submissionId,
      @AuthenticationPrincipal UserDetails userDetails) {

    UUID userId = UUID.fromString(userDetails.getUsername());
    submissionService.cancelSubmission(userId, submissionId);

    return ResponseEntity.noContent().build();
  }
}
