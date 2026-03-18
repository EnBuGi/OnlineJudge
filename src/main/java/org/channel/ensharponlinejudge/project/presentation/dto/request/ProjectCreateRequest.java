package org.channel.ensharponlinejudge.project.presentation.dto.request;

import java.time.LocalDateTime;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import org.channel.ensharponlinejudge.project.domain.ProjectType;

import lombok.Builder;

@Builder
public record ProjectCreateRequest(
    @NotBlank String title,
    @NotNull ProjectType type,
    @PositiveOrZero int generation,
    @NotNull LocalDateTime startDate,
    @NotNull LocalDateTime dueDate,
    String description,
    String skeletonUrl,
    String testCodeUrl,
    @Valid @NotNull ScorePolicyDto scorePolicy) {}
