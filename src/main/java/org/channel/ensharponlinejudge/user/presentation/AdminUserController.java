package org.channel.ensharponlinejudge.user.presentation;

import java.util.List;

import org.channel.ensharponlinejudge.user.presentation.dto.AdminUserResponse;
import org.channel.ensharponlinejudge.user.service.AdminUserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/users")
@PreAuthorize("hasRole('MENTOR')")
public class AdminUserController {

  private final AdminUserService adminUserService;

  @GetMapping
  public ResponseEntity<List<AdminUserResponse>> getUsers(
      @RequestParam(value = "generation", required = false) Integer generation) {
    return ResponseEntity.ok(adminUserService.getUsers(generation));
  }
}
