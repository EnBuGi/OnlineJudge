package org.channel.ensharponlinejudge.project.presentation;

import java.util.List;
import java.util.UUID;

import org.channel.ensharponlinejudge.project.presentation.dto.response.MenteeProjectDetailResponse;
import org.channel.ensharponlinejudge.project.presentation.dto.response.MenteeProjectListResponse;
import org.channel.ensharponlinejudge.project.service.ProjectService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/projects")
public class ProjectController {

  private final ProjectService projectService;

  @GetMapping
  public ResponseEntity<List<MenteeProjectListResponse>> getMenteeProjects(
      @AuthenticationPrincipal UserDetails userDetails) {
    UUID userId = UUID.fromString(userDetails.getUsername());
    List<MenteeProjectListResponse> responses = projectService.getMenteeProjects(userId);
    return ResponseEntity.ok(responses);
  }

  @GetMapping("/{id}")
  public ResponseEntity<MenteeProjectDetailResponse> getMenteeProjectDetail(
      @AuthenticationPrincipal UserDetails userDetails, @PathVariable("id") UUID projectId) {
    UUID userId = UUID.fromString(userDetails.getUsername());
    MenteeProjectDetailResponse response = projectService.getMenteeProjectDetail(userId, projectId);
    return ResponseEntity.ok(response);
  }
}
