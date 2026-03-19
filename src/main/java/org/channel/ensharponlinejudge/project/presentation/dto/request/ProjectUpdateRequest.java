package org.channel.ensharponlinejudge.project.presentation.dto.request;

import java.time.LocalDateTime;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import org.channel.ensharponlinejudge.project.domain.ProjectType;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Builder;

@Builder
public record ProjectUpdateRequest(
    @NotBlank String title,
    @NotNull ProjectType type,
    @PositiveOrZero int generation,
    @NotNull @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime startDate,
    @NotNull @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime dueDate,
    String description,
    String skeletonUrl,
    String testCodeUrl,
    String testCodeKey,
    @Valid @NotNull ScorePolicyDto scorePolicy) {}
