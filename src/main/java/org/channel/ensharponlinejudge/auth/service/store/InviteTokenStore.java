package org.channel.ensharponlinejudge.auth.service.store;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.channel.ensharponlinejudge.user.domain.Role;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class InviteTokenStore {

  private static final String INVITE_TOKEN_PREFIX = "invite:";
  private final StringRedisTemplate redisTemplate;

  public void saveInviteToken(String token, Role role, long durationInMillis) {
    redisTemplate
        .opsForValue()
        .set(INVITE_TOKEN_PREFIX + token, role.name(), durationInMillis, TimeUnit.MILLISECONDS);
  }

  public Optional<Role> getRole(String token) {
    String roleName = redisTemplate.opsForValue().get(INVITE_TOKEN_PREFIX + token);
    if (roleName != null) {
      return Optional.of(Role.valueOf(roleName));
    }
    return Optional.empty();
  }

  public void removeInviteToken(String token) {
    redisTemplate.delete(INVITE_TOKEN_PREFIX + token);
  }
}
