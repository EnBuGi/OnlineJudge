package org.channel.ensharponlinejudge.exception.enums;

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

  // Auth
  USER_ALREADY_EXISTS("A001", "이미 가입되어 있는 유저입니다.", HttpStatus.CONFLICT),
  INVALID_TOKEN("A002", "유효하지 않은 토큰입니다.", HttpStatus.UNAUTHORIZED),
  TOKEN_USER_MISMATCH("A003", "토큰의 유저 정보가 일치하지 않습니다.", HttpStatus.UNAUTHORIZED),
  LOGIN_FAILED("A004", "로그인에 실패했습니다.", HttpStatus.UNAUTHORIZED),

  // Member
  INVALID_PASSWORD_FORMAT("M001", "비밀번호는 암호화되어야 합니다.", HttpStatus.INTERNAL_SERVER_ERROR),
  ;

  private final String code;
  private final String message;
  private final HttpStatus status;
}
