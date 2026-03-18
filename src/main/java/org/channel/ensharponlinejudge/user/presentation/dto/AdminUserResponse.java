package org.channel.ensharponlinejudge.user.presentation.dto;

import java.util.UUID;

import org.channel.ensharponlinejudge.user.domain.Role;

import lombok.Builder;

@Builder
public record AdminUserResponse(
    UUID id, String name, String githubId, Role role, int generation, String profileImageUrl) {}
