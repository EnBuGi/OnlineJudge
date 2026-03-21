package org.channel.ensharponlinejudge.project.presentation.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import org.channel.ensharponlinejudge.project.domain.ProjectType;
import org.channel.ensharponlinejudge.project.presentation.dto.request.ScorePolicyDto;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Builder;

@Builder
public record AdminProjectDetailResponse(
    UUID id,
    String title,
    ProjectType type,
    int generation,
    String description,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime startDate,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime dueDate,
    String skeletonUrl,
    String testCodeKey,
    ScorePolicyDto scorePolicy) {}
