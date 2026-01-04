package org.channel.ensharponlinejudge.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

  // Common
  INVALID_INPUT_VALUE("C001", "유효하지 않은 입력 값입니다.", HttpStatus.BAD_REQUEST),
  METHOD_NOT_ALLOWED("C002", "허용되지 않은 메소드입니다.", HttpStatus.METHOD_NOT_ALLOWED),
  ENTITY_NOT_FOUND("C003", "엔티티를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
  INTERNAL_SERVER_ERROR("C004", "서버 에러", HttpStatus.INTERNAL_SERVER_ERROR),
  INVALID_TYPE_VALUE("C005", "유효하지 않은 타입 값입니다.", HttpStatus.BAD_REQUEST),
  HANDLE_ACCESS_DENIED("C006", "접근이 거부되었습니다.", HttpStatus.FORBIDDEN),
  ;

  private final String code;
  private final String message;
  private final HttpStatus status;
}
