package org.folio.pv.service.exception;

public class NoRulesMatchedException extends RuntimeException {
  public NoRulesMatchedException(String message) {
    super(message);
  }
}
