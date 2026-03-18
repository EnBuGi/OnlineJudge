package org.channel.ensharponlinejudge.exception.enums;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProjectErrorCode implements ErrorCode {
  ERR_FORBIDDEN("관리자 권한이 없습니다.", HttpStatus.FORBIDDEN),
  ERR_PROJECT_NOT_FOUND("해당 프로젝트를 찾을 수 없습니다.", HttpStatus.NOT_FOUND);

  private final String message;
  private final HttpStatus status;
}
