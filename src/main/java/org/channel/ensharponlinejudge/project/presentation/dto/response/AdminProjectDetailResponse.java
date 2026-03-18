package org.channel.ensharponlinejudge.project.presentation.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import org.channel.ensharponlinejudge.project.domain.ProjectType;
import org.channel.ensharponlinejudge.project.presentation.dto.request.ScorePolicyDto;

import lombok.Builder;

@Builder
public record AdminProjectDetailResponse(
    UUID id,
    String title,
    ProjectType type,
    int generation,
    String description,
    LocalDateTime startDate,
    LocalDateTime dueDate,
    String skeletonUrl,
    String testCodeUrl,
    ScorePolicyDto scorePolicy) {}
