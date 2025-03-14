package org.folio.pv.service.validator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.glytching.junit.extension.random.Random;
import io.github.glytching.junit.extension.random.RandomBeansExtension;
import java.util.stream.Stream;
import org.folio.pv.client.PwnedClient;
import org.folio.pv.domain.RuleType;
import org.folio.pv.domain.entity.PasswordValidationRule;
import org.folio.spring.FolioExecutionContext;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith({
  MockitoExtension.class,
  RandomBeansExtension.class
})
class ValidatorRegistryImplTest {

  @Mock
  private FolioExecutionContext folioExecutionContext;
  @Mock
  private ObjectMapper jacksonObjectMapper;
  @Mock
  private PwnedClient pwnedClient;

  @InjectMocks
  private ValidatorRegistryImpl registry;


  @Test
  void shouldFailWithSpecificNpeIfRuleIsNull() {
    var exception = assertThrows(NullPointerException.class, () -> registry.validatorByRule(null));
    assertThat(exception).hasMessage("Validation rule is null");
  }

  @Test
  void shouldFailWithNotRegisteredRuleType() {
    PasswordValidationRule unknown = new PasswordValidationRule();
    var exception = assertThrows(IllegalStateException.class, () -> registry.validatorByRule(unknown));
    assertThat(exception).hasMessage("Validator is not registered for rule type: null");
  }

  @Test
  void shouldFailIfRuleTypeIsInvalid(@Random String ruleType) {
    var exception = assertThrows(IllegalArgumentException.class, () -> {
      PasswordValidationRule rule = mockedRuleWithType(ruleType);
      registry.validatorByRule(rule);
    });
    assertThat(exception).hasMessageContaining("Unexpected value");
  }

  @ParameterizedTest
  @MethodSource("validatorPerRuleProvider")
  void shouldReturnTheCorrectValidatorPerRuleType(PasswordValidationRule rule,
                                                  Class<Validator> expectedValidatorClass) {
    Validator validator = registry.validatorByRule(rule);

    assertThat(validator).isInstanceOf(expectedValidatorClass);
  }

  private static Stream<Arguments> validatorPerRuleProvider() {
    return Stream.of(
      arguments(mockedRuleWithType(RuleType.REGEXP.getValue()), RegExpValidator.class),
      arguments(mockedRuleWithType(RuleType.PROGRAMMATIC.getValue()), ProgrammaticValidator.class),
      arguments(mockedRuleWithType(RuleType.PWNEDPASSWORD.getValue()), PwnedPasswordValidator.class)
    );
  }

  private static PasswordValidationRule mockedRuleWithType(String ruleType) {
    PasswordValidationRule rule = new PasswordValidationRule();
    rule.setRuleType(RuleType.fromValue(ruleType));

    return rule;
  }
}
