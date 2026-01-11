package org.channel.ensharponlinejudge.auth.service.store;

import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class TokenStore {

  @Getter
  private static final class TokenInfo {
    private final String value;
    private final long expirationTime;

    private TokenInfo(String value, long durationInMillis) {
      this.value = value;
      this.expirationTime = System.currentTimeMillis() + durationInMillis;
    }

    private boolean isExpired() {
      return System.currentTimeMillis() > expirationTime;
    }
  }

  // Key: Email, Value: TokenInfo (RefreshToken)
  private final Map<String, TokenInfo> refreshTokenStore = new ConcurrentHashMap<>();

  // Key: AccessToken, Value: ExpirationTime (Blacklist)
  private final Map<String, Long> blackListStore = new ConcurrentHashMap<>();

  // Refresh Token 저장
  public void saveRefreshToken(String email, String refreshToken, long durationInMillis) {
    refreshTokenStore.put(email, new TokenInfo(refreshToken, durationInMillis));
  }

  // Refresh Token 조회
  public Optional<String> getRefreshToken(String email) {
    TokenInfo tokenInfo = refreshTokenStore.get(email);
    if (tokenInfo != null && !tokenInfo.isExpired()) {
      return Optional.of(tokenInfo.getValue());
    }
    return Optional.empty();
  }

  // Refresh Token 삭제
  public void removeRefreshToken(String email) {
    refreshTokenStore.remove(email);
  }

  // Blacklist 추가
  public void addToBlacklist(String accessToken, long durationInMillis) {
    blackListStore.put(accessToken, System.currentTimeMillis() + durationInMillis);
  }

  // Blacklist 확인
  public boolean isBlacklisted(String accessToken) {
    Long expirationTime = blackListStore.get(accessToken);
    if (expirationTime == null) {
      return false;
    }
    if (System.currentTimeMillis() > expirationTime) {
      blackListStore.remove(accessToken); // 만료되었으면 조회 시점에 삭제
      return false;
    }
    return true;
  }

  // 1시간마다 만료된 토큰 정리
  @Scheduled(fixedRate = 3600000)
  public void cleanUp() {
    long now = System.currentTimeMillis();
    int removedRefreshTokenCount = 0;
    int removedBlacklistCount = 0;

    // Refresh Token 정리
    Iterator<Map.Entry<String, TokenInfo>> refreshIterator =
        refreshTokenStore.entrySet().iterator();
    while (refreshIterator.hasNext()) {
      if (refreshIterator.next().getValue().isExpired()) {
        refreshIterator.remove();
        removedRefreshTokenCount++;
      }
    }

    // Blacklist 정리
    Iterator<Map.Entry<String, Long>> blacklistIterator = blackListStore.entrySet().iterator();
    while (blacklistIterator.hasNext()) {
      if (now > blacklistIterator.next().getValue()) {
        blacklistIterator.remove();
        removedBlacklistCount++;
      }
    }

    if (removedRefreshTokenCount > 0 || removedBlacklistCount > 0) {
      log.info(
          "Expired tokens cleaned up: {} refresh tokens, {} blacklist tokens.",
          removedRefreshTokenCount,
          removedBlacklistCount);
    }
  }
}
