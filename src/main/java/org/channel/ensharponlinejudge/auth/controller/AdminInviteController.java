package org.channel.ensharponlinejudge.auth.controller;

import org.channel.ensharponlinejudge.auth.controller.dto.InviteRequest;
import org.channel.ensharponlinejudge.auth.controller.dto.InviteResponse;
import org.channel.ensharponlinejudge.auth.service.InviteService;
import org.channel.ensharponlinejudge.user.domain.Role;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "🔒 관리자 초대", description = "관리자 전용 초대 링크 생성 API")
@RestController
@RequestMapping("/api/v1/admin/invites")
@RequiredArgsConstructor
public class AdminInviteController {

  private final InviteService inviteService;

  @Operation(summary = "초대 링크 생성", description = "특정 역할(MENTOR/MENTEE)에 대한 기간 한정 초대 토큰을 생성합니다.")
  @PostMapping
  public ResponseEntity<InviteResponse> createInvite(@RequestBody InviteRequest request) {
    String token = inviteService.generateInviteToken(request.role(), request.durationInMillis());
    int generation = (request.role() == Role.MENTEE) ? inviteService.getCurrentGeneration() : 0;

    return ResponseEntity.ok(
        InviteResponse.builder().token(token).role(request.role()).generation(generation).build());
  }
}
