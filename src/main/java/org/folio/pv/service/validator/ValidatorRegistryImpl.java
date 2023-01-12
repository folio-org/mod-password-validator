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

  private final FolioExecutionContext folioExecutionContext;
  private final ObjectMapper jacksonObjectMapper;
  private final PwnedClient pwnedClient;


  @Override
  public Validator validatorByRule(@NonNull PasswordValidationRule rule) {
    Objects.requireNonNull(rule, "Validation rule is null");

    Validator validator;

    var ruleType = rule.getRuleType();

    if (ruleType == RuleType.RegExp) {
      validator = new RegExpValidator(rule);
    } else if (ruleType == RuleType.Programmatic) {
      validator = new ProgrammaticValidator(rule, folioExecutionContext, jacksonObjectMapper);
    } else if (ruleType == RuleType.PwnedPassword) {
      validator = new PwnedPasswordValidator(rule, pwnedClient);
    } else {
      throw new IllegalStateException("Validator is not registered for rule type: " + ruleType);
    }

    return validator;
  }

}
