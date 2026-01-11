package org.channel.ensharponlinejudge.auth.service.dtos;

import lombok.Builder;

@Builder
public record TokenDto(String accessToken, String refreshToken) {}
