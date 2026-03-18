package org.channel.ensharponlinejudge.auth.filter;

import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/** IP 기반 Rate Limiter — /api/v1/auth/** 엔드포인트에 적용됩니다. 분당 최대 10회 요청을 허용하며, 초과 시 HTTP 429를 반환합니다. */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

  private static final int MAX_REQUESTS_PER_MINUTE = 10;
  private static final long WINDOW_MILLIS = 60_000L; // 1분
  private static final String AUTH_PATH_PREFIX = "/api/v1/auth";

  private final ConcurrentHashMap<String, RateLimitEntry> rateLimitMap = new ConcurrentHashMap<>();

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String path = request.getRequestURI();
    return !path.startsWith(AUTH_PATH_PREFIX);
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    String clientIp = getClientIp(request);
    long now = Instant.now().toEpochMilli();

    RateLimitEntry entry =
        rateLimitMap.compute(
            clientIp,
            (ip, existing) -> {
              if (existing == null || now - existing.windowStart >= WINDOW_MILLIS) {
                return new RateLimitEntry(now, 1);
              }
              existing.count++;
              return existing;
            });

    if (entry.count > MAX_REQUESTS_PER_MINUTE) {
      response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
      response.setContentType(MediaType.APPLICATION_JSON_VALUE);
      response
          .getWriter()
          .write(
              "{\"status\":429,\"code\":\"RATE_LIMIT_EXCEEDED\",\"message\":\"요청이 너무 많습니다. 잠시 후 다시 시도하세요.\"}");
      return;
    }

    filterChain.doFilter(request, response);
  }

  private String getClientIp(HttpServletRequest request) {
    String xForwardedFor = request.getHeader("X-Forwarded-For");
    if (xForwardedFor != null && !xForwardedFor.isBlank()) {
      return xForwardedFor.split(",")[0].trim();
    }
    return request.getRemoteAddr();
  }

  private static class RateLimitEntry {
    long windowStart;
    int count;

    RateLimitEntry(long windowStart, int count) {
      this.windowStart = windowStart;
      this.count = count;
    }
  }
}
