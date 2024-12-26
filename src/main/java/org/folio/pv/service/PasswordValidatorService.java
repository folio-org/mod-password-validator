package org.folio.pv.service;

import org.folio.pv.domain.dto.Password;
import org.folio.pv.domain.dto.PasswordCheck;
import org.folio.pv.domain.dto.ValidationResult;

public interface PasswordValidatorService {
  ValidationResult validatePasswordByRules(String tenant, Password passwordContainer);

  ValidationResult checkPassword(String tenant, PasswordCheck passwordCheck);
}
