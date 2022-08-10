package org.folio.pv.service.validator;

import org.folio.pv.domain.entity.PasswordValidationRule;
import org.springframework.lang.NonNull;

public interface ValidatorRegistry {

  Validator validatorByRule(@NonNull PasswordValidationRule rule);

}
