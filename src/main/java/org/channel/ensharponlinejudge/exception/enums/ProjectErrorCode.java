package org.channel.ensharponlinejudge.exception.enums;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProjectErrorCode implements ErrorCode {
  ERR_FORBIDDEN("관리자 권한이 없습니다.", HttpStatus.FORBIDDEN),
  ERR_PROJECT_NOT_FOUND("해당 프로젝트를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
  ERR_FILE_PARSE_FAILED("파일 파싱에 실패했습니다.", HttpStatus.BAD_REQUEST),
  ERR_INVALID_FILE_EXTENSION("테스트 코드 파일은 .zip 형식이어야 합니다.", HttpStatus.BAD_REQUEST),
  ERR_FILE_SIZE_EXCEEDED("파일 크기가 허용 한도를 초과했습니다.", HttpStatus.BAD_REQUEST),
  ERR_INVALID_FILE_STRUCTURE("src/test 이외의 폴더가 존재하거나 src/test 폴더가 없습니다.", HttpStatus.BAD_REQUEST),
  ERR_TEMP_FILE_NOT_FOUND("임시 파일을 찾을 수 없습니다.", HttpStatus.NOT_FOUND);

  private final String message;
  private final HttpStatus status;
}
