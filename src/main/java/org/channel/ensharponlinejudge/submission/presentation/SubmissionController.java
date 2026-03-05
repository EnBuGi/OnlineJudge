package org.channel.ensharponlinejudge.submission.presentation;

import java.util.UUID;

import jakarta.validation.Valid;

import org.channel.ensharponlinejudge.submission.presentation.dto.request.SubmitRequest;
import org.channel.ensharponlinejudge.submission.presentation.dto.response.SubmitResponse;
import org.channel.ensharponlinejudge.submission.service.SubmissionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/submissions")
public class SubmissionController {

  private final SubmissionService submissionService;

  @PostMapping
  public ResponseEntity<SubmitResponse> submit(@Valid @RequestBody SubmitRequest submitRequest) {
    UUID submissionId =
        submissionService.submit(
            submitRequest.userId(), submitRequest.projectId(), submitRequest.repoUrl());

    return ResponseEntity.status(201).body(new SubmitResponse(submissionId));
  }
}
