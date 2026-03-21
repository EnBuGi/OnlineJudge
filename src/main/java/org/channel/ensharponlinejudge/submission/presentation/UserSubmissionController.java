package org.channel.ensharponlinejudge.submission.presentation;

import java.util.UUID;

import org.channel.ensharponlinejudge.submission.presentation.dto.response.MyGlobalSubmissionResponse;
import org.channel.ensharponlinejudge.submission.service.SubmissionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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
  public ResponseEntity<Page<MyGlobalSubmissionResponse>> getMyGlobalSubmissions(
      @AuthenticationPrincipal UserDetails userDetails,
      @PageableDefault(
              size = 10,
              sort = "submittedAt",
              direction = org.springframework.data.domain.Sort.Direction.DESC)
          Pageable pageable) {

    UUID userId = UUID.fromString(userDetails.getUsername());
    return ResponseEntity.ok(submissionService.getMyGlobalSubmissions(userId, pageable));
  }
}
