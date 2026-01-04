package org.channel.ensharponlinejudge.exception;

import org.channel.ensharponlinejudge.exception.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  protected ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
      MethodArgumentNotValidException e) {
    ErrorCode errorCode = ErrorCode.INVALID_INPUT_VALUE;
    ErrorResponse response =
        new ErrorResponse(
            errorCode.getStatus().value(),
            errorCode.name(),
            errorCode.getCode(),
            errorCode.getMessage());
    return new ResponseEntity<>(response, HttpStatus.valueOf(errorCode.getStatus().value()));
  }

  @ExceptionHandler(BusinessException.class)
  protected ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {
    ErrorCode errorCode = e.getErrorCode();
    ErrorResponse response =
        new ErrorResponse(
            errorCode.getStatus().value(),
            errorCode.name(),
            errorCode.getCode(),
            errorCode.getMessage());
    return new ResponseEntity<>(response, HttpStatus.valueOf(errorCode.getStatus().value()));
  }

  @ExceptionHandler(Exception.class)
  protected ResponseEntity<ErrorResponse> handleException(Exception e) {
    ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
    ErrorResponse response =
        new ErrorResponse(
            errorCode.getStatus().value(),
            errorCode.name(),
            errorCode.getCode(),
            errorCode.getMessage());
    return new ResponseEntity<>(response, HttpStatus.valueOf(errorCode.getStatus().value()));
  }
}
