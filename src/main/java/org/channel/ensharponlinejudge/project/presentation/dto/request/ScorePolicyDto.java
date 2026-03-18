package org.channel.ensharponlinejudge.project.presentation.dto.request;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import lombok.Builder;

@Builder
public record ScorePolicyDto(
    @Positive int timeLimit, @Positive int memoryLimit, @Valid @NotNull List<TestCaseDto> cases) {}
