package org.channel.ensharponlinejudge.submission.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record SubmitRequest(
    @NotBlank
        @Pattern(
            regexp = "^https?://(www\\.)?github\\.com/[A-Za-z0-9_.-]+/[A-Za-z0-9_.-]+/?(\\.git)?$",
            message = "repoUrl이 git repository url 형태가 아닙니다.")
        String repoUrl) {}
