package org.channel.ensharponlinejudge.exception.response;

import java.time.LocalDateTime;

import lombok.Getter;

@Getter
public class ErrorResponse {

  private final LocalDateTime timestamp = LocalDateTime.now();
  private final int status;
  private final String error;
  private final String code;
  private final String message;

  public ErrorResponse(int status, String error, String code, String message) {
    this.status = status;
    this.error = error;
    this.code = code;
    this.message = message;
  }
}
