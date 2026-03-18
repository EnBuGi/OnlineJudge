package org.channel.ensharponlinejudge.project.presentation;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;

import org.channel.ensharponlinejudge.project.presentation.dto.request.ProjectCreateRequest;
import org.channel.ensharponlinejudge.project.presentation.dto.request.ProjectUpdateRequest;
import org.channel.ensharponlinejudge.project.presentation.dto.response.AdminProjectDetailResponse;
import org.channel.ensharponlinejudge.project.presentation.dto.response.AdminProjectListResponse;
import org.channel.ensharponlinejudge.project.presentation.dto.response.ProjectCreateResponse;
import org.channel.ensharponlinejudge.project.service.ProjectService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/projects")
public class ProjectAdminController {

  private final ProjectService projectService;

  @PostMapping
  public ResponseEntity<ProjectCreateResponse> createProject(
      @RequestParam("userId") UUID userId, // TODO: JWT 적용 후 제거
      @Valid @RequestBody ProjectCreateRequest request) {

    UUID projectId = projectService.createProject(userId, request);
    return ResponseEntity.status(201).body(new ProjectCreateResponse(projectId));
  }

  @PutMapping("/{id}")
  public ResponseEntity<Void> updateProject(
      @RequestParam("userId") UUID userId, // TODO: JWT 적용 후 제거
      @PathVariable("id") UUID projectId,
      @Valid @RequestBody ProjectUpdateRequest request) {

    projectService.updateProject(userId, projectId, request);
    return ResponseEntity.ok().build();
  }

  @DeleteMapping("/project/{id}")
  public ResponseEntity<Void> deleteProject(
      @RequestParam("userId") UUID userId, // TODO: JWT 적용 후 제거
      @PathVariable("id") UUID projectId) {

    projectService.deleteProject(userId, projectId);
    return ResponseEntity.noContent().build();
  }

  @GetMapping
  public ResponseEntity<List<AdminProjectListResponse>> getAdminProjects(
      @RequestParam("userId") UUID userId // TODO: JWT 적용 후 제거
      ) {

    List<AdminProjectListResponse> responses = projectService.getAdminProjects(userId);
    return ResponseEntity.ok(responses);
  }

  @GetMapping("/{id}")
  public ResponseEntity<AdminProjectDetailResponse> getAdminProjectDetail(
      @RequestParam("userId") UUID userId, // TODO: JWT 적용 후 제거
      @PathVariable("id") UUID projectId) {

    AdminProjectDetailResponse response = projectService.getAdminProjectDetail(userId, projectId);
    return ResponseEntity.ok(response);
  }
}
