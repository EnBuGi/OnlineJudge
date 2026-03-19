package org.channel.ensharponlinejudge.user.presentation.dto;

import jakarta.validation.constraints.NotNull;

import org.channel.ensharponlinejudge.user.domain.Role;

public record AdminUserRoleUpdateRequest(@NotNull Role role) {}
