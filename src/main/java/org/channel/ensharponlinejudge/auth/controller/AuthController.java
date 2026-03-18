package org.channel.ensharponlinejudge.auth.controller;

import jakarta.servlet.http.HttpServletResponse;

import org.channel.ensharponlinejudge.auth.controller.requests.GithubLoginRequest;
import org.channel.ensharponlinejudge.auth.controller.requests.GithubSignupRequest;
import org.channel.ensharponlinejudge.auth.controller.responses.GithubLoginInfoResponse;
import org.channel.ensharponlinejudge.auth.service.AuthService;
import org.channel.ensharponlinejudge.auth.service.dtos.AccessTokenResponse;
import org.channel.ensharponlinejudge.auth.service.dtos.TokenDto;
import org.channel.ensharponlinejudge.exception.BusinessException;
import org.channel.ensharponlinejudge.exception.enums.AuthErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
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

  @Value("${jwt.cookie.refresh-token-max-age-seconds:1209600}")
  private long refreshTokenMaxAgeSeconds;

  @Value("${jwt.cookie.secure:true}")
  private boolean refreshTokenCookieSecure;

  @Value("${jwt.cookie.same-site:Strict}")
  private String refreshTokenCookieSameSite;

  @Value("${jwt.cookie.path:/}")
  private String refreshTokenCookiePath;

  @Operation(
      summary = "GitHub 로그인 URL 조회",
      description = "안전한 생성을 위해 state가 포함된 GitHub 로그인 URL을 반환합니다.")
  @GetMapping("/auth/login/github/url")
  public ResponseEntity<String> getGithubLoginUrl(@RequestParam String redirectUri) {
    return ResponseEntity.ok(authService.getGithubLoginUrl(redirectUri));
  }

  @Operation(summary = "GitHub 로그인", description = "GitHub Code를 받아 로그인 또는 회원가입 필요 정보를 응답합니다.")
  @PostMapping("/auth/login/github")
  public ResponseEntity<?> loginGithub(
      @RequestBody GithubLoginRequest request, HttpServletResponse response) {
    Object result = authService.loginGithub(request);

    if (result instanceof GithubLoginInfoResponse) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
    } else {
      TokenDto tokenDto = (TokenDto) result;
      ResponseCookie cookie =
          setRefreshTokenCookie(
              tokenDto.refreshToken(),
              refreshTokenMaxAgeSeconds > 0 ? refreshTokenMaxAgeSeconds : 1209600);
      response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
      return ResponseEntity.ok(AccessTokenResponse.from(tokenDto.accessToken()));
    }
  }

  @Operation(summary = "GitHub 추가정보 기입 회원가입", description = "추가 정보를 입력받고 가입을 진행합니다.")
  @PostMapping("/auth/signup/github")
  public ResponseEntity<AccessTokenResponse> signupGithub(
      @RequestBody GithubSignupRequest request, HttpServletResponse response) {
    TokenDto tokenDto = authService.signupGithub(request);

    ResponseCookie cookie =
        setRefreshTokenCookie(
            tokenDto.refreshToken(),
            refreshTokenMaxAgeSeconds > 0 ? refreshTokenMaxAgeSeconds : 1209600);
    response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

    return ResponseEntity.status(HttpStatus.CREATED)
        .body(AccessTokenResponse.from(tokenDto.accessToken()));
  }

  @Operation(
      summary = "로그아웃",
      description = "사용자 로그아웃을 처리합니다. Access Token을 만료시켜 더 이상 사용할 수 없게 합니다.")
  @DeleteMapping("/auth/token")
  public ResponseEntity<String> logout(
      @Parameter(hidden = true) @RequestHeader("Authorization") String accessToken,
      HttpServletResponse response) {
    authService.logout(resolveToken(accessToken));

    ResponseCookie cookie =
        ResponseCookie.from("refresh_token", "")
            .httpOnly(true)
            .secure(refreshTokenCookieSecure)
            .path(refreshTokenCookiePath)
            .maxAge(0)
            .sameSite(refreshTokenCookieSameSite)
            .build();
    response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

    return ResponseEntity.ok("로그아웃 성공");
  }

  @Operation(
      summary = "Access Token 재발급",
      description = "유효한 Refresh Token을 사용하여 새로운 Access Token과 Refresh Token을 발급합니다.")
  @PostMapping("/auth/token/reissue")
  public ResponseEntity<AccessTokenResponse> reissue(
      @CookieValue("refresh_token") String refreshToken, HttpServletResponse response) {
    TokenDto tokenDto = authService.reissue(refreshToken);

    ResponseCookie cookie =
        setRefreshTokenCookie(
            tokenDto.refreshToken(),
            refreshTokenMaxAgeSeconds > 0 ? refreshTokenMaxAgeSeconds : 1209600);
    response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

    return ResponseEntity.ok(AccessTokenResponse.from(tokenDto.accessToken()));
  }

  private String resolveToken(String authorizationHeader) {
    if (StringUtils.hasText(authorizationHeader) && authorizationHeader.startsWith("Bearer ")) {
      return authorizationHeader.substring(7);
    }
    throw new BusinessException(AuthErrorCode.INVALID_TOKEN);
  }

  private ResponseCookie setRefreshTokenCookie(String refreshToken, long maxAge) {
    return ResponseCookie.from("refresh_token", refreshToken)
        .httpOnly(true)
        .secure(refreshTokenCookieSecure)
        .path(refreshTokenCookiePath)
        .maxAge(maxAge)
        .sameSite(refreshTokenCookieSameSite)
        .build();
  }
}
