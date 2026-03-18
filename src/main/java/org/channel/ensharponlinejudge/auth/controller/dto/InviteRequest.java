package org.channel.ensharponlinejudge.auth.controller.dto;

import org.channel.ensharponlinejudge.user.domain.Role;

public record InviteRequest(Role role, long durationInMillis) {}
