package org.channel.ensharponlinejudge.auth.service;

import jakarta.transaction.Transactional;

import org.channel.ensharponlinejudge.auth.controller.requests.LoginRequest;
import org.channel.ensharponlinejudge.auth.controller.requests.SignupRequest;
import org.channel.ensharponlinejudge.auth.service.dtos.TokenDto;
import org.channel.ensharponlinejudge.auth.service.store.TokenStore;
import org.channel.ensharponlinejudge.domain.member.domain.Member;
import org.channel.ensharponlinejudge.domain.member.repository.MemberRepository;
import org.channel.ensharponlinejudge.exception.BusinessException;
import org.channel.ensharponlinejudge.exception.enums.AuthErrorCode;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

  // Refresh Token 유효 기간: 14일 (ms)
  private static final long REFRESH_TOKEN_VALIDITY_MS = 14L * 24 * 60 * 60 * 1000;

  private final MemberRepository memberRepository;
  private final PasswordEncoder passwordEncoder;
  private final AuthenticationManager authenticationManager;
  private final JwtTokenProvider jwtTokenProvider;
  private final TokenStore tokenStore;

  @Transactional
  public void signup(SignupRequest request) {
    if (memberRepository.existsByEmail(request.email())) {
      throw new BusinessException(AuthErrorCode.USER_ALREADY_EXISTS);
    }
    Member member = Member.initialize(request.email(), passwordEncoder.encode(request.password()));
    memberRepository.save(member);
  }

  public TokenDto login(LoginRequest request) {
    // 1. Email/PW 기반 Authentication 객체 생성
    UsernamePasswordAuthenticationToken authenticationToken =
        new UsernamePasswordAuthenticationToken(request.email(), request.password());

    // 2. 검증 (사용자 비밀번호 체크)
    Authentication authentication = authenticationManager.authenticate(authenticationToken);

    // 3. 토큰 발급 및 저장 (공통 로직 분리)
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

  public void withdraw(String accessToken, String password) {
    // 1. Access Token 검증 및 Authentication 조회
    if (!jwtTokenProvider.validateToken(accessToken)) {
      throw new BusinessException(AuthErrorCode.INVALID_TOKEN);
    }
    Authentication authentication = jwtTokenProvider.getAuthentication(accessToken);

    // 2. 사용자 조회
    Member member = memberRepository.findByEmail(authentication.getName());
    if (member == null) {
      throw new BusinessException(AuthErrorCode.USER_NOT_FOUND);
    }

    // 3. 비밀번호 확인
    if (!passwordEncoder.matches(password, member.getPassword())) {
      throw new BusinessException(AuthErrorCode.PASSWORD_MISMATCH);
    }

    // 4. 회원 탈퇴 (Soft Delete)
    memberRepository.delete(member);

    // 5. 로그아웃 처리 (토큰 무효화)
    logout(accessToken);
  }

  // 토큰 생성 및 저장 로직 추출 (Login, Reissue 공통 사용)
  private TokenDto issueTokens(Authentication authentication) {
    String accessToken = jwtTokenProvider.createAccessToken(authentication);
    String refreshToken = jwtTokenProvider.createRefreshToken(authentication);

    tokenStore.saveRefreshToken(authentication.getName(), refreshToken, REFRESH_TOKEN_VALIDITY_MS);

    return TokenDto.builder().accessToken(accessToken).refreshToken(refreshToken).build();
  }
}
