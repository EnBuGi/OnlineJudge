package org.channel.ensharponlinejudge.project.presentation.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import org.channel.ensharponlinejudge.project.domain.ProjectType;

import lombok.Builder;

@Builder
public record MenteeProjectListResponse(
    UUID id, String title, ProjectType type, LocalDateTime startDate, LocalDateTime dueDate) {}
