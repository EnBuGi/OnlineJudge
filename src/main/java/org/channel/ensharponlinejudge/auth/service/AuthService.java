package org.channel.ensharponlinejudge.auth.service;

import java.util.concurrent.TimeUnit;

import jakarta.transaction.Transactional;

import org.channel.ensharponlinejudge.auth.controller.requests.LoginRequest;
import org.channel.ensharponlinejudge.auth.controller.requests.SignupRequest;
import org.channel.ensharponlinejudge.auth.service.dtos.TokenDto;
import org.channel.ensharponlinejudge.domain.member.entity.Member;
import org.channel.ensharponlinejudge.domain.member.repository.MemberRepository;
import org.channel.ensharponlinejudge.exception.BusinessException;
import org.channel.ensharponlinejudge.exception.enums.ErrorCode;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

  private final MemberRepository memberRepository;
  private final PasswordEncoder passwordEncoder;
  private final AuthenticationManagerBuilder authenticationManagerBuilder;
  private final JwtTokenProvider jwtTokenProvider;
  private final RedisTemplate<String, String> redisTemplate;

  @Transactional
  public void signup(SignupRequest request) {
    if (memberRepository.existsByEmail(request.email())) {
      throw new BusinessException(ErrorCode.USER_ALREADY_EXISTS);
    }
    Member member = Member.initialize(request.email(), passwordEncoder.encode(request.password()));
    memberRepository.save(member);
  }

  @Transactional
  public TokenDto login(LoginRequest request) {
    // 1. Email/PW 기반 Authentication 객체 생성
    UsernamePasswordAuthenticationToken authenticationToken =
        new UsernamePasswordAuthenticationToken(request.email(), request.password());

    // 2. 검증 (사용자 비밀번호 체크)
    Authentication authentication =
        authenticationManagerBuilder.getObject().authenticate(authenticationToken);

    // 3. 토큰 생성
    String accessToken = jwtTokenProvider.createAccessToken(authentication);
    String refreshToken = jwtTokenProvider.createRefreshToken(authentication);

    // 4. Redis에 Refresh Token 저장 (Key: "RT:{email}", Value: refreshToken, Timeout: 2주)
    redisTemplate
        .opsForValue()
        .set(
            "RT:" + authentication.getName(),
            refreshToken,
            14, // 2주
            TimeUnit.DAYS);

    return TokenDto.builder().accessToken(accessToken).refreshToken(refreshToken).build();
  }

  @Transactional
  public void logout(String accessToken) {
    // 1. Access Token 검증
    if (!jwtTokenProvider.validateToken(accessToken)) {
      throw new BusinessException(ErrorCode.INVALID_TOKEN);
    }

    Authentication authentication = jwtTokenProvider.getAuthentication(accessToken);

    // 2. Redis에서 해당 유저의 Refresh Token 삭제
    if (redisTemplate.opsForValue().get("RT:" + authentication.getName()) != null) {
      redisTemplate.delete("RT:" + authentication.getName());
    }

    // 3. 해당 Access Token을 BlackList로 등록 (TTL: Access Token의 남은 시간)
    Long expiration = jwtTokenProvider.getExpiration(accessToken);
    redisTemplate.opsForValue().set(accessToken, "logout", expiration, TimeUnit.MILLISECONDS);
  }

  @Transactional
  public TokenDto reissue(String refreshToken) {
    // 1. Refresh Token 검증
    if (!jwtTokenProvider.validateToken(refreshToken)) {
      throw new BusinessException(ErrorCode.INVALID_TOKEN);
    }

    // 2. 토큰에서 유저 ID(Email) 추출 -> Redis에 저장된 RT와 일치하는지 확인
    Authentication authentication = jwtTokenProvider.getAuthentication(refreshToken);
    String redisRefreshToken = redisTemplate.opsForValue().get("RT:" + authentication.getName());

    if (!refreshToken.equals(redisRefreshToken)) {
      throw new BusinessException(ErrorCode.TOKEN_USER_MISMATCH);
    }

    // 3. 새로운 토큰 생성 및 Redis 업데이트 (RT Rotation)
    String newAccessToken = jwtTokenProvider.createAccessToken(authentication);
    String newRefreshToken = jwtTokenProvider.createRefreshToken(authentication);

    redisTemplate
        .opsForValue()
        .set("RT:" + authentication.getName(), newRefreshToken, 14, TimeUnit.DAYS);

    return TokenDto.builder().accessToken(newAccessToken).refreshToken(newRefreshToken).build();
  }
}
