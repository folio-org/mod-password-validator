package org.folio.pv.testutils;

import io.github.benas.randombeans.EnhancedRandomBuilder;
import io.github.benas.randombeans.api.EnhancedRandom;
import io.github.benas.randombeans.randomizers.text.StringRandomizer;
import java.nio.charset.StandardCharsets;
import lombok.Value;
import org.apache.commons.lang3.StringUtils;
import org.folio.pv.domain.RuleType;
import org.folio.pv.domain.entity.PasswordValidationRule;
import org.folio.spring.FolioModuleMetadata;

public final class RandomTestData {

  private static final EnhancedRandom RULE_RANDOMIZER;
  private static final StringRandomizer MODULE_NAME_RANDOMIZER;

  static {
    RULE_RANDOMIZER = EnhancedRandomBuilder.aNewEnhancedRandomBuilder()
      .objectPoolSize(100)
      .charset(StandardCharsets.UTF_8)
      .stringLengthRange(5, 50)
      .overrideDefaultInitialization(false)
      .build();

    MODULE_NAME_RANDOMIZER = StringRandomizer.aNewStringRandomizer(10);
  }

  private RandomTestData() {
  }

  public static PasswordValidationRule nextRandomRuleOfType(RuleType type) {
    PasswordValidationRule result = RULE_RANDOMIZER.nextObject(PasswordValidationRule.class);
    result.setRuleType(type.getValue());

    return result;
  }

  public static FolioModuleMetadata nextRandomModuleMetadata() {
    return new FolioModuleMetadataImpl(MODULE_NAME_RANDOMIZER.getRandomValue());
  }

  @Value
  private static class FolioModuleMetadataImpl implements FolioModuleMetadata {

    String moduleName;

    @Override
    public String getDBSchemaName(String tenantId) {
      if (StringUtils.isBlank(tenantId)) {
        throw new IllegalArgumentException("tenantId can't be null or empty");
      }

      return tenantId + (StringUtils.isNotBlank(moduleName) ? "_" + moduleName.toLowerCase().replace('-', '_') : "");
    }
  }
}
