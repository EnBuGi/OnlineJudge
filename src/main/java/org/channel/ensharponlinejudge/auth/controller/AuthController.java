package org.channel.ensharponlinejudge.auth.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import org.channel.ensharponlinejudge.auth.controller.requests.LoginRequest;
import org.channel.ensharponlinejudge.auth.controller.requests.SignupRequest;
import org.channel.ensharponlinejudge.auth.controller.requests.WithdrawRequest;
import org.channel.ensharponlinejudge.auth.service.AuthService;
import org.channel.ensharponlinejudge.auth.service.dtos.AccessTokenResponse;
import org.channel.ensharponlinejudge.auth.service.dtos.TokenDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "🔐 인증/인가", description = "사용자 회원가입, 로그인, 로그아웃 및 토큰 관련 API")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;

  @Value("${jwt.cookie.refresh-token-max-age-seconds}")
  private long refreshTokenMaxAgeSeconds;

  @Operation(summary = "회원가입", description = "새로운 사용자를 등록합니다.")
  @PostMapping("/members")
  public ResponseEntity<String> signup(@Valid @RequestBody SignupRequest request) {
    authService.signup(request);
    return ResponseEntity.status(HttpStatus.CREATED).body("회원가입 성공");
  }

  @Operation(
      summary = "로그인",
      description = "사용자 로그인 후 Access Token과 Refresh Token을 발급합니다. Refresh Token은 쿠키에 저장됩니다.")
  @PostMapping("/auth/token")
  public ResponseEntity<AccessTokenResponse> login(
      @Valid @RequestBody LoginRequest request, HttpServletResponse response) {
    TokenDto tokenDto = authService.login(request);

    ResponseCookie cookie = createRefreshTokenCookie(tokenDto.refreshToken());
    response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

    return ResponseEntity.ok(AccessTokenResponse.from(tokenDto.accessToken()));
  }

  @Operation(
      summary = "로그아웃",
      description = "사용자 로그아웃을 처리합니다. Access Token을 만료시켜 더 이상 사용할 수 없게 합니다.")
  @DeleteMapping("/auth/token")
  public ResponseEntity<String> logout(
      @Parameter(hidden = true) @RequestHeader("Authorization") String accessToken) {
    authService.logout(accessToken.substring(7));
    return ResponseEntity.ok("로그아웃 성공");
  }

  @Operation(
      summary = "Access Token 재발급",
      description = "유효한 Refresh Token을 사용하여 새로운 Access Token과 Refresh Token을 발급합니다.")
  @PostMapping("/auth/token/reissue")
  public ResponseEntity<AccessTokenResponse> reissue(
      @CookieValue("refresh_token") String refreshToken, HttpServletResponse response) {
    TokenDto tokenDto = authService.reissue(refreshToken);

    ResponseCookie cookie = createRefreshTokenCookie(tokenDto.refreshToken());
    response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

    return ResponseEntity.ok(AccessTokenResponse.from(tokenDto.accessToken()));
  }

  @Operation(
      summary = "회원 탈퇴",
      description = "회원 탈퇴를 처리합니다. 비밀번호 확인 후 회원은 삭제 상태로 변경되며, 관련 토큰은 만료됩니다.")
  @DeleteMapping("/members")
  public ResponseEntity<String> withdraw(
      @Parameter(hidden = true) @RequestHeader("Authorization") String accessToken,
      @Valid @RequestBody WithdrawRequest request) {
    authService.withdraw(accessToken.substring(7), request.password());
    return ResponseEntity.ok("회원 탈퇴 성공");
  }

  private ResponseCookie createRefreshTokenCookie(String refreshToken) {
    return ResponseCookie.from("refresh_token", refreshToken)
        .httpOnly(true)
        .secure(true)
        .path("/")
        .maxAge(refreshTokenMaxAgeSeconds)
        .sameSite("Strict")
        .build();
  }
}
