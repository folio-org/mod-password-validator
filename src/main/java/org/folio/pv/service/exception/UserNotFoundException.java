package org.folio.pv.service.exception;

import org.springframework.http.HttpStatus;

public class UserNotFoundException extends RuntimeHttpException {

  private static final String MESSAGE = "User is not found: id = ";
  private static final HttpStatus STATUS_CODE = HttpStatus.NOT_FOUND;

  public UserNotFoundException(String userId) {
    super(MESSAGE + userId, STATUS_CODE);
  }
}
