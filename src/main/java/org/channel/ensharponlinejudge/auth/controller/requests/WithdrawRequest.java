package org.channel.ensharponlinejudge.auth.controller.requests;

import jakarta.validation.constraints.NotBlank;

public record WithdrawRequest(@NotBlank(message = "비밀번호는 필수입니다.") String password) {}
