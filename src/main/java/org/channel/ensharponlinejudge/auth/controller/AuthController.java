package org.channel.ensharponlinejudge.auth.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.channel.ensharponlinejudge.auth.controller.requests.LoginRequest;
import org.channel.ensharponlinejudge.auth.service.AuthService;
import org.channel.ensharponlinejudge.auth.service.dtos.TokenResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;

  @PostMapping("/signup")
  public ResponseEntity<String> signup(@RequestBody LoginRequest request) {
    authService.signup(request);
    return ResponseEntity.ok("회원가입 성공");
  }

  @PostMapping("/login")
  public ResponseEntity<TokenResponse> login(@RequestBody LoginRequest request, HttpServletResponse response) {
    TokenResponse tokenResponse = authService.login(request);

    // Refresh Token을 HttpOnly Cookie에 담아 설정
    ResponseCookie cookie = createRefreshTokenCookie(tokenResponse.refreshToken());

    response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

    // Access Token만 Body로 반환
    return ResponseEntity.ok(TokenResponse.builder()
        .accessToken(tokenResponse.accessToken())
        .refreshToken(null) // Body에는 RT를 싣지 않음
        .build());
  }

  @PostMapping("/logout")
  public ResponseEntity<String> logout(@RequestHeader("Authorization") String accessToken,
                                       @CookieValue("refresh_token") String refreshToken) {
    authService.logout(accessToken.substring(7), refreshToken);
    return ResponseEntity.ok("로그아웃 성공");
  }

  @PostMapping("/reissue")
  public ResponseEntity<TokenResponse> reissue(@CookieValue("refresh_token") String refreshToken, HttpServletResponse response) {
    TokenResponse tokenResponse = authService.reissue(refreshToken);

    // 재발급된 Refresh Token 쿠키 재설정
    ResponseCookie cookie = createRefreshTokenCookie(tokenResponse.refreshToken());

    response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

    return ResponseEntity.ok(TokenResponse.builder()
        .accessToken(tokenResponse.accessToken())
        .refreshToken(null)
        .build());
  }

  private ResponseCookie createRefreshTokenCookie(String refreshToken) {
    return ResponseCookie.from("refresh_token", refreshToken)
        .httpOnly(true)
        .secure(true) // HTTPS 환경에서만 전송 (로컬 개발 시 false로 변경 필요할 수 있음)
        .path("/")
        .maxAge(14 * 24 * 60 * 60) // 2주
        .sameSite("Strict") // CSRF 방어 강화
        .build();
  }
}
