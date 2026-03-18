package org.channel.ensharponlinejudge.auth.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.channel.ensharponlinejudge.auth.controller.requests.GithubLoginRequest;
import org.channel.ensharponlinejudge.auth.controller.requests.GithubSignupRequest;
import org.channel.ensharponlinejudge.auth.controller.responses.GithubLoginInfoResponse;
import org.channel.ensharponlinejudge.auth.infra.github.GithubOAuthClient;
import org.channel.ensharponlinejudge.auth.service.dtos.TokenDto;
import org.channel.ensharponlinejudge.auth.service.store.TokenStore;
import org.channel.ensharponlinejudge.exception.BusinessException;
import org.channel.ensharponlinejudge.exception.enums.AuthErrorCode;
import org.channel.ensharponlinejudge.user.domain.Role;
import org.channel.ensharponlinejudge.user.domain.User;
import org.channel.ensharponlinejudge.user.repository.UserRepository;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

  private final UserRepository memberRepository;
  private final JwtTokenProvider jwtTokenProvider;
  private final TokenStore tokenStore;
  private final GithubOAuthClient githubOAuthClient;
  private final InviteService inviteService;

  @Transactional
  public Object loginGithub(GithubLoginRequest request) {
    // state 검증 (CSRF 방지)
    if (!tokenStore.isValidState(request.getState())) {
      throw new BusinessException(AuthErrorCode.INVALID_OAUTH_STATE);
    }

    String accessToken = githubOAuthClient.getAccessToken(request.getCode());
    Map<String, Object> userInfo = githubOAuthClient.getUserInfo(accessToken);
    String githubId = String.valueOf(userInfo.get("id")); // id is unique and numeric.
    String profileImageUrl = (String) userInfo.get("avatar_url");

    Optional<User> userOpt = memberRepository.findByGithubId(githubId);
    if (userOpt.isPresent()) {
      User user = userOpt.get();
      Authentication authentication =
          new UsernamePasswordAuthenticationToken(
              user.getGithubId(), null, List.of(new SimpleGrantedAuthority(user.getRole().name())));
      return issueTokens(authentication);
    } else {
      return new GithubLoginInfoResponse("회원가입이 필요합니다.", githubId, profileImageUrl);
    }
  }

  @Transactional
  public TokenDto signupGithub(GithubSignupRequest request) {
    if (memberRepository.existsByGithubId(request.getGithubId())) {
      throw new BusinessException(AuthErrorCode.USER_ALREADY_EXISTS);
    }

    Role role = inviteService.validateToken(request.getInviteToken());

    int currentGeneration = inviteService.getCurrentGeneration();
    if (role == Role.MENTEE && request.getGeneration() != currentGeneration) {
      throw new BusinessException(AuthErrorCode.GENERATION_MISMATCH);
    }
    if (role == Role.MENTOR && request.getGeneration() == currentGeneration) {
      throw new BusinessException(AuthErrorCode.GENERATION_MISMATCH);
    }

    User user =
        User.builder()
            .githubId(request.getGithubId())
            .name(request.getName())
            .generation(request.getGeneration())
            .role(role)
            .profileImageUrl(request.getProfileImageUrl())
            .isDeleted(false)
            .build();

    memberRepository.save(user);

    // After signup, remove the invite token
    inviteService.removeInviteToken(request.getInviteToken());

    Authentication authentication =
        new UsernamePasswordAuthenticationToken(
            user.getGithubId(), null, List.of(new SimpleGrantedAuthority(user.getRole().name())));
    return issueTokens(authentication);
  }

  public void logout(String accessToken) {
    // 1. Access Token 검증
    if (!jwtTokenProvider.validateToken(accessToken)) {
      throw new BusinessException(AuthErrorCode.INVALID_TOKEN);
    }

    Authentication authentication = jwtTokenProvider.getAuthentication(accessToken);

    // 2. TokenStore에서 해당 유저의 Refresh Token 삭제
    tokenStore.removeRefreshToken(authentication.getName());

    // 3. 해당 Access Token을 BlackList로 등록 (TTL: Access Token의 남은 시간)
    long expiration = jwtTokenProvider.getExpiration(accessToken);
    tokenStore.addToBlacklist(accessToken, expiration);
  }

  public TokenDto reissue(String refreshToken) {
    // 1. Refresh Token 검증
    if (!jwtTokenProvider.validateToken(refreshToken)) {
      throw new BusinessException(AuthErrorCode.INVALID_TOKEN);
    }

    Authentication authentication = jwtTokenProvider.getAuthentication(refreshToken);

    // 2. TokenStore에 저장된 RT 조회 (없으면 예외 발생)
    // orElseThrow를 사용하여 null 처리를 방지하고, 토큰이 없으면 즉시 '유효하지 않은 토큰' 예외 발생
    String storedRefreshToken =
        tokenStore
            .getRefreshToken(authentication.getName())
            .orElseThrow(() -> new BusinessException(AuthErrorCode.INVALID_TOKEN));

    // 3. 토큰 일치 여부 확인
    if (!refreshToken.equals(storedRefreshToken)) {
      throw new BusinessException(AuthErrorCode.TOKEN_USER_MISMATCH);
    }

    // 4. 토큰 재발급 및 저장 (RT Rotation)
    return issueTokens(authentication);
  }

  // 토큰 생성 및 저장 로직 추출 (Login, Reissue 공통 사용)
  public TokenDto issueTokens(Authentication authentication) {
    String accessToken = jwtTokenProvider.createAccessToken(authentication);
    String refreshToken = jwtTokenProvider.createRefreshToken(authentication);

    tokenStore.saveRefreshToken(
        authentication.getName(), refreshToken, jwtTokenProvider.getRefreshTokenValidity());

    return TokenDto.builder().accessToken(accessToken).refreshToken(refreshToken).build();
  }

  public String getGithubLoginUrl(String redirectUri) {
    String state = UUID.randomUUID().toString();
    tokenStore.saveState(state);

    return "https://github.com/login/oauth/authorize?"
        + "client_id="
        + githubOAuthClient.getClientId()
        + "&redirect_uri="
        + redirectUri
        + "&state="
        + state
        + "&scope=user:email";
  }
}
