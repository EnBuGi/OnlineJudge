package org.channel.ensharponlinejudge.exception.enums;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SubmissionErrorCode implements ErrorCode {
  SUBMISSION_NOT_FOUND("해당 제출을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),

  PROJECT_NOT_FOUND("해당 프로젝트를 찾을 수 없습니다", HttpStatus.NOT_FOUND),

  USER_NOT_FOUND("해당 유저를 찾을 수 없습니다", HttpStatus.NOT_FOUND),

  SUBMISSION_IN_PROGRESS("현재 채점이 진행 중입니다", HttpStatus.CONFLICT),

  QUEUE_PUBLISH_FAILED("큐 전송을 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),

  TEST_CASE_NOT_FOUND("테스트 케이스가 존재하지 않습니다.", HttpStatus.BAD_REQUEST);

  private final String message;
  private final HttpStatus status;
}
