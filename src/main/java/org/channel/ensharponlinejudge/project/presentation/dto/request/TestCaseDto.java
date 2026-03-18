package org.channel.ensharponlinejudge.project.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import lombok.Builder;

@Builder
public record TestCaseDto(
    String id, @NotBlank String name, @PositiveOrZero int score, @NotNull Boolean isHidden) {}
