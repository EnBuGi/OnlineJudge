package org.channel.ensharponlinejudge.auth.filter;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.channel.ensharponlinejudge.auth.service.JwtTokenProvider;
import org.channel.ensharponlinejudge.auth.service.store.TokenStore;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtTokenProvider jwtTokenProvider;
  private final TokenStore tokenStore;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    String token = resolveToken(request);

    // 1. 토큰 유효성 검사
    if (token != null && jwtTokenProvider.validateToken(token)) {
      // 2. TokenStore에서 해당 Access Token이 로그아웃(Blacklist)된 상태인지 확인
      if (!tokenStore.isBlacklisted(token)) {
        try {
          // 3. 로그아웃 상태가 아니라면 토큰으로 인증 정보 조회 후 SecurityContext에 저장
          Authentication authentication = jwtTokenProvider.getAuthentication(token);
          SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (Exception e) {
          log.error("Authentication failed for token: {}", token, e);
          SecurityContextHolder.clearContext();
        }
      }
    }
    filterChain.doFilter(request, response);
  }

  // Header에서 "Bearer {token}" 형태 추출
  private String resolveToken(HttpServletRequest request) {
    String bearerToken = request.getHeader("Authorization");
    if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
      return bearerToken.substring(7);
    }
    return null;
  }
}
