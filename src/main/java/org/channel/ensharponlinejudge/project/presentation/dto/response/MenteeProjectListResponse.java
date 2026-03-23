package org.channel.ensharponlinejudge.project.presentation.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import org.channel.ensharponlinejudge.project.domain.ProjectType;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Builder;

@Builder
public record MenteeProjectListResponse(
    UUID id,
    String title,
    ProjectType type,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime startDate,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime dueDate,
    boolean canSubmit) {}
