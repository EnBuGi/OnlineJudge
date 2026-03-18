package org.channel.ensharponlinejudge.auth.service;

import java.time.LocalDate;
import java.util.UUID;

import org.channel.ensharponlinejudge.auth.service.store.InviteTokenStore;
import org.channel.ensharponlinejudge.exception.BusinessException;
import org.channel.ensharponlinejudge.exception.enums.AuthErrorCode;
import org.channel.ensharponlinejudge.user.domain.Role;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InviteService {

  private final InviteTokenStore inviteTokenStore;

  public String generateInviteToken(Role role, long durationInMillis) {
    String token = UUID.randomUUID().toString();
    inviteTokenStore.saveInviteToken(token, role, durationInMillis);
    return token;
  }

  public Role validateToken(String token) {
    return inviteTokenStore
        .getRole(token)
        .orElseThrow(() -> new BusinessException(AuthErrorCode.INVALID_INVITE_TOKEN));
  }

  public void removeInviteToken(String token) {
    inviteTokenStore.removeInviteToken(token);
  }

  public int getCurrentGeneration() {
    return LocalDate.now().getYear() - 2000;
  }
}
