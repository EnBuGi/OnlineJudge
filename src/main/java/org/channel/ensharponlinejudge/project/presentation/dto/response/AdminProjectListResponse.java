package org.channel.ensharponlinejudge.project.presentation.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import org.channel.ensharponlinejudge.project.domain.ProjectType;

import lombok.Builder;

@Builder
public record AdminProjectListResponse(
    UUID id,
    String title,
    ProjectType type,
    int generation,
    LocalDateTime startDate,
    LocalDateTime dueDate) {}
