package org.channel.ensharponlinejudge.exception;

import org.channel.ensharponlinejudge.exception.enums.AuthErrorCode;
import org.channel.ensharponlinejudge.exception.enums.CommonErrorCode;
import org.channel.ensharponlinejudge.exception.enums.ErrorCode;
import org.channel.ensharponlinejudge.exception.response.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  protected ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
      MethodArgumentNotValidException e) {
    ErrorCode errorCode = CommonErrorCode.INVALID_INPUT_VALUE;
    ErrorResponse response = createErrorResponse(errorCode);
    return new ResponseEntity<>(response, errorCode.getStatus());
  }

  @ExceptionHandler(BusinessException.class)
  protected ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {
    ErrorCode errorCode = e.getErrorCode();
    ErrorResponse response = createErrorResponse(errorCode);
    return new ResponseEntity<>(response, errorCode.getStatus());
  }

  @ExceptionHandler(AuthenticationException.class)
  protected ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException e) {
    ErrorCode errorCode = AuthErrorCode.LOGIN_FAILED;
    ErrorResponse response = createErrorResponse(errorCode);
    return new ResponseEntity<>(response, errorCode.getStatus());
  }

  @ExceptionHandler(Exception.class)
  protected ResponseEntity<ErrorResponse> handleException(Exception e) {
    ErrorCode errorCode = CommonErrorCode.INTERNAL_SERVER_ERROR;
    ErrorResponse response = createErrorResponse(errorCode);
    return new ResponseEntity<>(response, errorCode.getStatus());
  }

  private ErrorResponse createErrorResponse(ErrorCode errorCode) {
    return new ErrorResponse(
        errorCode.getStatus().value(), errorCode.name(), errorCode.getMessage());
  }
}
