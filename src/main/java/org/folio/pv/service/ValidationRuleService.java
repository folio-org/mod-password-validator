package org.folio.pv.service;

import java.util.List;
import org.folio.pv.domain.dto.ValidationRule;
import org.folio.pv.domain.dto.ValidationRuleCollection;
import org.folio.pv.domain.entity.PasswordValidationRule;

public interface ValidationRuleService {

  ValidationRule getValidationRuleById(String ruleId);

  ValidationRuleCollection getValidationRules(Integer offset, Integer limit, String cql);

  ValidationRule createOrUpdateValidationRule(ValidationRule validationRule);

  ValidationRule storeValidationRule(ValidationRule validationRule);

  List<PasswordValidationRule> getEnabledRules(String tenant);
}
