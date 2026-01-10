package org.channel.ensharponlinejudge.auth.controller.responses;

import lombok.Builder;

@Builder
public record AccessTokenResponse(String accessToken) {
    public static AccessTokenResponse from(String accessToken) {
        return AccessTokenResponse.builder()
                .accessToken(accessToken)
                .build();
    }
}
