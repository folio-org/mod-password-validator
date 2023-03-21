package org.folio.pv.service.validator;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.pv.client.PwnedClient;
import org.folio.pv.domain.RuleType;
import org.folio.pv.domain.entity.PasswordValidationRule;
import org.folio.spring.FolioExecutionContext;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Log4j2
class ValidatorRegistryImpl implements ValidatorRegistry {

  private static final String VALIDATOR_NOT_FOUND_ERROR = "Validator is not registered for rule type: %s";
  private final FolioExecutionContext folioExecutionContext;
  private final ObjectMapper jacksonObjectMapper;
  private final PwnedClient pwnedClient;


  @Override
  public Validator validatorByRule(@NonNull PasswordValidationRule rule) {
    Objects.requireNonNull(rule, "Validation rule is null");

    Validator validator;

    var ruleType = rule.getRuleType();
    log.info("validatorByRule:: ruleType is {}", rule.getRuleType());

    if (ruleType == RuleType.REGEXP) {
      validator = new RegExpValidator(rule);
    } else if (ruleType == RuleType.PROGRAMMATIC) {
      validator = new ProgrammaticValidator(rule, folioExecutionContext, jacksonObjectMapper);
    } else if (ruleType == RuleType.PWNEDPASSWORD) {
      validator = new PwnedPasswordValidator(rule, pwnedClient);
    } else {
      var errorMessage = String.format(VALIDATOR_NOT_FOUND_ERROR, ruleType);
      IllegalStateException e = new IllegalStateException(errorMessage);
      log.warn("Failed on creating validator, msg: {}", errorMessage);
      throw e;
    }

    return validator;
  }

}
