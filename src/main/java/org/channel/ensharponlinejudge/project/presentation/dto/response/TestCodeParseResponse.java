package org.channel.ensharponlinejudge.project.presentation.dto.response;

import java.util.List;

import lombok.Builder;

@Builder
public record TestCodeParseResponse(
    List<String> methodNames, String testCodeKey, String testCodeUrl) {}
