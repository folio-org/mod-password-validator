package org.folio.pv.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.pv.domain.dto.Password;
import org.folio.pv.domain.dto.PasswordCheck;
import org.folio.pv.domain.dto.ValidationResult;
import org.folio.pv.rest.resource.PasswordApi;
import org.folio.pv.service.PasswordValidatorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Log4j2
@RestController
@RequestMapping(value = "/")
@RequiredArgsConstructor
public class PasswordValidatorController implements PasswordApi {
  private final PasswordValidatorService passwordValidatorService;

  @Override
  public ResponseEntity<ValidationResult> validatePassword(@Valid Password password, String xOkapiTenant) {
    log.info("Validating password API");
    ValidationResult validationResult = passwordValidatorService.validatePasswordByRules(xOkapiTenant, password);
    return ResponseEntity.ok(validationResult);
  }

  @Override
  public ResponseEntity<ValidationResult> checkPassword(@Valid PasswordCheck passwordCheck, String xOkapiTenant) {
    log.info("Checking password API");
    ValidationResult validationResult = passwordValidatorService.checkPassword(xOkapiTenant, passwordCheck);
    return ResponseEntity.ok(validationResult);
  }
}
