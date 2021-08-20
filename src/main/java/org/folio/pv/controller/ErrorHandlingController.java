package org.folio.pv.controller;

import org.folio.pv.domain.dto.Error;
import org.folio.pv.domain.dto.Errors;
import org.folio.pv.domain.dto.Parameter;
import org.folio.pv.service.exception.UserNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;
import java.util.stream.Collectors;

@ControllerAdvice
public class ErrorHandlingController {
  private static final String NOT_FOUND_TYPE = "Not found";
  private static final String MISSING_FIELD_TYPE = "Field is missing";
  private static final String NOT_FOUND_CODE = "404";
  private static final String UNPROCESSABLE_ENTITY_CODE = "422";

  private static final String USER_ID_FIELD = "userId";


  @ResponseBody
  @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public Errors handleException(MethodArgumentNotValidException exception) {
    final List<Error> errors = exception.getBindingResult().getFieldErrors().stream()
      .map(error -> {
        Parameter parameter = new Parameter()
          .key(error.getField())
          .value(String.valueOf(error.getRejectedValue()));

        return new Error().message(error.getField() + ' ' + error.getDefaultMessage())
          .addParametersItem(parameter)
          .type(MISSING_FIELD_TYPE)
          .code(UNPROCESSABLE_ENTITY_CODE);
      })
      .collect(Collectors.toList());

    return new Errors().errors(errors).totalRecords(errors.size());
  }

  @ResponseBody
  @ResponseStatus(HttpStatus.NOT_FOUND)
  @ExceptionHandler(UserNotFoundException.class)
  public Error handleException(UserNotFoundException exception) {
    Parameter parameter = new Parameter()
      .key(USER_ID_FIELD)
      .value(exception.getUserId());

    return new Error().message(exception.getMessage())
      .type(NOT_FOUND_TYPE)
      .code(NOT_FOUND_CODE)
      .addParametersItem(parameter);
  }
}
