package org.folio.pv.controller;

import static feign.Util.UTF_8;

import java.util.List;
import java.util.stream.Collectors;

import feign.FeignException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import org.folio.pv.domain.dto.Error;
import org.folio.pv.domain.dto.Errors;
import org.folio.pv.domain.dto.Parameter;
import org.folio.pv.service.exception.UserNotFoundException;

@ControllerAdvice
public class ErrorHandlingController {

  private static final String USER_ID_FIELD = "userId";
  private static final String INTERNAL_ERROR_TYPE = "-1";
  private static final String FOLIO_EXTERNAL_OR_UNDEFINED_ERROR_TYPE = "-2";

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
          .type(INTERNAL_ERROR_TYPE);
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
      .type(FOLIO_EXTERNAL_OR_UNDEFINED_ERROR_TYPE)
      .addParametersItem(parameter);
  }

  @ResponseBody
  @ResponseStatus(HttpStatus.NOT_FOUND)
  @ExceptionHandler({FeignException.NotFound.class})
  public Error handleException(FeignException.NotFound exception) {
    var errorMessage = exception.responseBody()
      .map(byteBuffer -> new String(byteBuffer.array(), UTF_8))
      .orElse("User with given id not found");
    Parameter parameter = new Parameter()
      .key(USER_ID_FIELD);

    return new Error().message(errorMessage)
      .type(FOLIO_EXTERNAL_OR_UNDEFINED_ERROR_TYPE)
      .addParametersItem(parameter);
  }
}
