package org.channel.ensharponlinejudge.submission.presentation;

import java.util.UUID;

import jakarta.validation.Valid;

import org.channel.ensharponlinejudge.submission.presentation.dto.request.SubmitRequest;
import org.channel.ensharponlinejudge.submission.presentation.dto.response.SubmitResponse;
import org.channel.ensharponlinejudge.submission.service.SubmissionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
      @RequestParam("userId") UUID userId, // TODO: JWT 적용 후 제거
      @Valid @RequestBody SubmitRequest submitRequest) {
    UUID submissionId = submissionService.submit(userId, projectId, submitRequest.repoUrl());

    return ResponseEntity.status(201).body(new SubmitResponse(submissionId));
  }
}
