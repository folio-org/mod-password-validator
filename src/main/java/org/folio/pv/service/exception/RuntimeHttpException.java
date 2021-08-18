package org.folio.pv.service.exception;

import org.springframework.http.HttpStatus;

public class RuntimeHttpException extends RuntimeException {

  private final HttpStatus statusCode;

  public RuntimeHttpException(String message, HttpStatus responseStatusCode) {
    super(message);
    this.statusCode = responseStatusCode;
  }

  public HttpStatus getStatusCode() {
    return statusCode;
  }
}
