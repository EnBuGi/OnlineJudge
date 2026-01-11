package org.channel.ensharponlinejudge.exception.enums;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements ErrorCode {
  USER_ALREADY_EXISTS("이미 가입되어 있는 유저입니다.", HttpStatus.CONFLICT),

  INVALID_TOKEN("유효하지 않은 토큰입니다.", HttpStatus.UNAUTHORIZED),

  TOKEN_USER_MISMATCH("토큰의 유저 정보가 일치하지 않습니다.", HttpStatus.UNAUTHORIZED),

  LOGIN_FAILED("로그인에 실패했습니다.", HttpStatus.UNAUTHORIZED),

  INVALID_PASSWORD_FORMAT("비밀번호는 암호화되어야 합니다.", HttpStatus.INTERNAL_SERVER_ERROR),

  USER_NOT_FOUND("가입되지 않은 유저입니다.", HttpStatus.NOT_FOUND),

  PASSWORD_MISMATCH("비밀번호가 일치하지 않습니다.", HttpStatus.UNAUTHORIZED),
  ;

  private final String message;

  private final HttpStatus status;
}
