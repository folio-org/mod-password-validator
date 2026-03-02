package org.folio.pv.service.validator;

import org.folio.pv.domain.entity.PasswordValidationRule;
import org.jspecify.annotations.NonNull;

public interface ValidatorRegistry {

  Validator validatorByRule(@NonNull PasswordValidationRule rule);

}
