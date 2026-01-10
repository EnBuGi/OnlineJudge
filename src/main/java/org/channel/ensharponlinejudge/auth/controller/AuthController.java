package org.channel.ensharponlinejudge.auth.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.channel.ensharponlinejudge.auth.controller.requests.LoginRequest;
import org.channel.ensharponlinejudge.auth.controller.requests.SignupRequest;
import org.channel.ensharponlinejudge.auth.controller.responses.AccessTokenResponse;
import org.channel.ensharponlinejudge.auth.service.AuthService;
import org.channel.ensharponlinejudge.auth.service.dtos.TokenDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;

  @Value("${jwt.cookie.refresh-token-max-age-seconds}")
  private long refreshTokenMaxAgeSeconds;

  @PostMapping("/members")
  public ResponseEntity<String> signup(@RequestBody SignupRequest request) {
    authService.signup(request);
    return ResponseEntity.ok("회원가입 성공");
  }

  @PostMapping("/auth/token")
  public ResponseEntity<AccessTokenResponse> login(@RequestBody LoginRequest request, HttpServletResponse response) {
    TokenDto tokenDto = authService.login(request);

    ResponseCookie cookie = createRefreshTokenCookie(tokenDto.refreshToken());
    response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

    return ResponseEntity.ok(AccessTokenResponse.from(tokenDto.accessToken()));
  }

  @DeleteMapping("/auth/token")
  public ResponseEntity<String> logout(@RequestHeader("Authorization") String accessToken) {
    authService.logout(accessToken.substring(7));
    return ResponseEntity.ok("로그아웃 성공");
  }

  @PostMapping("/auth/token/reissue")
  public ResponseEntity<AccessTokenResponse> reissue(@CookieValue("refresh_token") String refreshToken, HttpServletResponse response) {
    TokenDto tokenDto = authService.reissue(refreshToken);

    ResponseCookie cookie = createRefreshTokenCookie(tokenDto.refreshToken());
    response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

    return ResponseEntity.ok(AccessTokenResponse.from(tokenDto.accessToken()));
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
