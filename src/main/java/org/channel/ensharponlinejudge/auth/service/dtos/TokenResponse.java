package org.channel.ensharponlinejudge.auth.service.dtos;

import lombok.Builder;

@Builder
public record TokenResponse(String accessToken, String refreshToken) {
}
