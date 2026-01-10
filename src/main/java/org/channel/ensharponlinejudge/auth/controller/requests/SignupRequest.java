package org.channel.ensharponlinejudge.auth.controller.requests;

import jakarta.validation.constraints.NotBlank;

public record SignupRequest(
        @NotBlank String email,
        @NotBlank String password
) {
}