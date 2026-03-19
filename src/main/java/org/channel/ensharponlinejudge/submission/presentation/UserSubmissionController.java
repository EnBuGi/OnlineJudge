package org.channel.ensharponlinejudge.submission.presentation;

import java.util.List;
import java.util.UUID;

import org.channel.ensharponlinejudge.submission.presentation.dto.response.MyGlobalSubmissionResponse;
import org.channel.ensharponlinejudge.submission.service.SubmissionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/submissions")
public class UserSubmissionController {

  private final SubmissionService submissionService;

  @GetMapping("/me")
  public ResponseEntity<List<MyGlobalSubmissionResponse>> getMyGlobalSubmissions(
      @AuthenticationPrincipal UserDetails userDetails) {

    UUID userId = UUID.fromString(userDetails.getUsername());
    return ResponseEntity.ok(submissionService.getMyGlobalSubmissions(userId));
  }
}
