package org.channel.ensharponlinejudge.user.presentation.dto;

import java.util.UUID;
import lombok.Builder;
import org.channel.ensharponlinejudge.user.domain.Role;
import org.channel.ensharponlinejudge.user.domain.User;

@Builder
public record UserResponse(
    UUID id,
    String githubId,
    String name,
    int generation,
    Role role,
    String profileImageUrl
) {
    public static UserResponse from(User user) {
        return UserResponse.builder()
            .id(user.getId())
            .githubId(user.getGithubId())
            .name(user.getName())
            .generation(user.getGeneration())
            .role(user.getRole())
            .profileImageUrl(user.getProfileImageUrl())
            .build();
    }
}
