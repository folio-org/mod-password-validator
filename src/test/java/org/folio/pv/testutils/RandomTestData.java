package org.folio.pv.testutils;

import java.nio.charset.StandardCharsets;

import io.github.benas.randombeans.EnhancedRandomBuilder;
import io.github.benas.randombeans.api.EnhancedRandom;

import org.folio.pv.domain.RuleType;
import org.folio.pv.domain.entity.PasswordValidationRule;

public class RandomTestData {

  private static final EnhancedRandom ruleRandomizer;

  static {
    ruleRandomizer = EnhancedRandomBuilder.aNewEnhancedRandomBuilder()
        .objectPoolSize(100)
        .charset(StandardCharsets.UTF_8)
        .stringLengthRange(5, 50)
        .overrideDefaultInitialization(false)
        .build();
  }

  private RandomTestData() {
  }

  public static PasswordValidationRule nextRandomRuleOfType(RuleType type) {
    PasswordValidationRule result = ruleRandomizer.nextObject(PasswordValidationRule.class);
    result.setRuleType(type.getValue());

    return result;
  }
}