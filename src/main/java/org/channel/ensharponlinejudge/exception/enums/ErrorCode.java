package org.channel.ensharponlinejudge.exception.enums;

import org.springframework.http.HttpStatus;

/**
 * Represents a contract for all error codes in the application. This approach allows for
 * domain-specific error code enums, improving modularity and maintainability. Each domain (e.g.,
 * Auth, Member) can have its own enum implementing this interface.
 */
public interface ErrorCode {

  String name();

  String getMessage();

  HttpStatus getStatus();
}
