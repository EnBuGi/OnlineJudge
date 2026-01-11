package org.channel.ensharponlinejudge.auth.service;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class JwtTokenProvider {

  private final SecretKey key;
  private final long accessTokenValidity;
  private final long refreshTokenValidity;

  public JwtTokenProvider(
      @Value("${jwt.secret}") String secret,
      @Value("${jwt.access-expiration}") long accessTokenValidity,
      @Value("${jwt.refresh-expiration}") long refreshTokenValidity) {
    if (secret == null || secret.isBlank()) {
      throw new IllegalArgumentException("JWT 비밀 키가 설정되지 않았습니다.");
    }

    byte[] keyBytes;
    try {
      keyBytes = Decoders.BASE64.decode(secret);
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("JWT 비밀 키가 유효한 Base64 형식이 아닙니다.", e);
    }

    this.key = Keys.hmacShaKeyFor(keyBytes);
    this.accessTokenValidity = accessTokenValidity;
    this.refreshTokenValidity = refreshTokenValidity;
  }

  // Access Token 생성
  public String createAccessToken(Authentication authentication) {
    return createToken(authentication, accessTokenValidity);
  }

  // Refresh Token 생성
  public String createRefreshToken(Authentication authentication) {
    return createToken(authentication, refreshTokenValidity);
  }

  private String createToken(Authentication authentication, long validity) {
    String authorities =
        authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.joining(","));

    // Modern Java: Use Instant instead of Date for calculation
    Instant now = Instant.now();
    Date validityDate = Date.from(now.plusMillis(validity));

    return Jwts.builder()
        .subject(authentication.getName())
        .claim("auth", authorities)
        .signWith(key)
        .expiration(validityDate) // JJWT still uses java.util.Date for API
        .compact();
  }

  // 토큰에서 인증 정보 추출
  public Authentication getAuthentication(String token) {
    Claims claims = parseClaims(token);

    if (claims.get("auth") == null) {
      throw new IllegalArgumentException("권한 정보가 없는 토큰입니다.");
    }

    Collection<? extends GrantedAuthority> authorities =
        Arrays.stream(claims.get("auth").toString().split(","))
            .filter(auth -> !auth.isBlank())
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toList());

    UserDetails principal = new User(claims.getSubject(), "", authorities);
    return new UsernamePasswordAuthenticationToken(principal, "", authorities);
  }

  // 토큰 유효성 검증
  public boolean validateToken(String token) {
    try {
      Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
      return true;
    } catch (RuntimeException e) { // JJWT exceptions extend RuntimeException
      switch (e) {
        case SecurityException _, MalformedJwtException _ -> log.info("잘못된 JWT 서명입니다.");
        case ExpiredJwtException _ -> log.info("만료된 JWT 토큰입니다.");
        case UnsupportedJwtException _ -> log.info("지원되지 않는 JWT 토큰입니다.");
        case IllegalArgumentException _ -> log.info("JWT 토큰이 잘못되었습니다.");
        default -> log.info("알 수 없는 JWT 오류입니다: {}", e.getMessage());
      }
    }
    return false;
  }

  // 남은 유효시간 계산 (로그아웃 시 블랙리스트 TTL 설정용)
  public long getExpiration(String token) {
    Date expiration = parseClaims(token).getExpiration();
    long remaining = expiration.toInstant().toEpochMilli() - System.currentTimeMillis();
    return Math.max(0, remaining);
  }

  private Claims parseClaims(String token) {
    try {
      return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    } catch (ExpiredJwtException e) {
      return e.getClaims();
    }
  }
}
