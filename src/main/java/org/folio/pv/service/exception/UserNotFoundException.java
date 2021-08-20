package org.folio.pv.service.exception;

public class UserNotFoundException extends RuntimeException {

  private static final String MESSAGE = "User with given id not found";
  private final String userId;

  public UserNotFoundException(String userId) {
    super(MESSAGE);
    this.userId = userId;
  }

  public String getUserId() {
    return userId;
  }
}
