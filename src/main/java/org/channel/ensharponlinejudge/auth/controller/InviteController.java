package org.channel.ensharponlinejudge.auth.controller;

import org.channel.ensharponlinejudge.auth.controller.dto.InviteResponse;
import org.channel.ensharponlinejudge.auth.service.InviteService;
import org.channel.ensharponlinejudge.user.domain.Role;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "🤝 초대", description = "초대 토큰 검증 API")
@RestController
@RequestMapping("/api/v1/invites")
@RequiredArgsConstructor
public class InviteController {

  private final InviteService inviteService;

  @Operation(summary = "초대 토큰 검증", description = "초대 토큰이 유효한지 확인하고 관련 정보(역할, 기수)를 반환합니다.")
  @GetMapping("/validate")
  public ResponseEntity<InviteResponse> validateInvite(@RequestParam("token") String token) {
    Role role = inviteService.validateToken(token);
    int generation = (role == Role.MENTEE) ? inviteService.getCurrentGeneration() : 0;

    return ResponseEntity.ok(
        InviteResponse.builder().token(token).role(role).generation(generation).build());
  }
}
