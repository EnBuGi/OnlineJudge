package org.channel.ensharponlinejudge.auth.controller.dto;

import org.channel.ensharponlinejudge.user.domain.Role;

import lombok.Builder;

@Builder
public record InviteResponse(String token, Role role, int generation) {}
