package org.folio.pv.service.exception;

import org.springframework.http.HttpStatus;

public class FieldNotProvidedException extends RuntimeHttpException {

  private static final String MESSAGE = " is not provided";
  private static final HttpStatus STATUS_CODE = HttpStatus.BAD_REQUEST;

  public FieldNotProvidedException(String field) {
    super(field + MESSAGE, STATUS_CODE);
  }
}

