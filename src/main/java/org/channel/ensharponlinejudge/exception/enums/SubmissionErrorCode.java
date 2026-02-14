package org.channel.ensharponlinejudge.exception.enums;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SubmissionErrorCode implements ErrorCode {
  PROJECT_NOT_FOUND("해당 프로젝트를 찾을 수 없습니다", HttpStatus.NOT_FOUND),

  USER_NOT_FOUND("해당 유저를 찾을 수 없습니다", HttpStatus.NOT_FOUND),

  SUBMISSION_IN_PROGRESS("현재 채점이 진행 중입니다", HttpStatus.CONFLICT);

  private final String message;
  private final HttpStatus status;
}
