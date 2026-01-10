package org.channel.ensharponlinejudge.auth.service.dtos;

import lombok.Builder;

@Builder
public record AccessTokenResponse(String accessToken) {
  public static AccessTokenResponse from(String accessToken) {
    return AccessTokenResponse.builder().accessToken(accessToken).build();
  }
}
