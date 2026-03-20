package org.channel.ensharponlinejudge.user.presentation;

import java.util.UUID;

import org.channel.ensharponlinejudge.user.presentation.dto.UserResponse;
import org.channel.ensharponlinejudge.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "👤 사용자", description = "사용자 프로필 관련 API")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;

  @Operation(summary = "내 프로필 조회", description = "현재 로그인한 사용자의 정보를 조회합니다.")
  @GetMapping("/me")
  public ResponseEntity<UserResponse> getMyProfile(
      @AuthenticationPrincipal UserDetails userDetails) {
    UUID userId = UUID.fromString(userDetails.getUsername());
    return ResponseEntity.ok(userService.getUserProfile(userId));
  }
}
