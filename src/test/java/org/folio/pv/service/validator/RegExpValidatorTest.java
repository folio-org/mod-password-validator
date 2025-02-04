package org.folio.pv.service.validator;

import static io.github.benas.randombeans.randomizers.text.StringRandomizer.aNewStringRandomizer;
import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.pv.testutils.RandomTestData.nextRandomRuleOfType;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.glytching.junit.extension.random.Random;
import io.github.glytching.junit.extension.random.RandomBeansExtension;
import org.folio.pv.domain.RuleType;
import org.folio.pv.domain.dto.UserData;
import org.folio.pv.domain.dto.ValidationErrors;
import org.folio.pv.domain.entity.PasswordValidationRule;
import org.folio.spring.test.type.UnitTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;

@UnitTest
@ExtendWith(RandomBeansExtension.class)
class RegExpValidatorTest {

  @Random
  private UserData userData;

  private PasswordValidationRule rule;
  private RegExpValidator validator;


  @BeforeEach
  void setUp() {
    rule = nextRandomRuleOfType(RuleType.REGEXP);

    validator = new RegExpValidator(rule);
  }

  @ParameterizedTest
  @NullAndEmptySource
  void shouldPassWithEmptyExpression(String expression) {
    rule.setRuleExpression(expression);

    String password = aNewStringRandomizer().getRandomValue();
    ValidationErrors errors = validator.validate(password, userData);

    assertFalse(errors.hasErrors());
  }

  @Test
  void shouldPassIfPasswordMatchesExpression() {
    rule.setRuleExpression(".*valid-password.*");

    String password = "valid-password-123";
    ValidationErrors errors = validator.validate(password, userData);

    assertFalse(errors.hasErrors());
  }

  @Test
  void shouldReplaceUserNamePlaceholderInExpressionBeforeMatching() {
    rule.setRuleExpression(".*valid-password-<USER_NAME>.*");

    String password = "valid-password-user1";
    UserData user1Data = new UserData("1", "user1");
    ValidationErrors errors = validator.validate(password, user1Data);

    assertFalse(errors.hasErrors());
  }

  @Test
  void shouldReturnErrorWithMessageIdIfNoMatch() {
    rule.setRuleExpression(".*valid-password.*");

    String password = "not-a-password";
    ValidationErrors errors = validator.validate(password, userData);

    Assertions.assertAll(
      () -> assertTrue(errors.hasErrors()),
      () -> assertThat(errors.getErrorMessages()).containsExactly(rule.getErrMessageId())
    );
  }

  @Test
  void shouldReturnErrorWithMessageIdIfNoMatchForSpace() {
    //rule.setRuleExpression("^(?:(?!<USER_NAME>).)+$");
    rule.setRuleExpression("^(?i)(?:(?!<USER_NAME>).)+$");

    String password = "Us ername3.";
    UserData user1Data = new UserData("1", "Username3.");
    ValidationErrors errors = validator.validate(password, user1Data);

    Assertions.assertAll(
      () -> assertTrue(errors.hasErrors()),
      () -> assertThat(errors.getErrorMessages()).containsExactly(rule.getErrMessageId())
    );
  }

  @Test
  void shouldReturnErrorWithMessageIdIfNoMatchForUpperAndLowerCase() {
    //rule.setRuleExpression("^(?:(?!<USER_NAME>).)+$");
    rule.setRuleExpression("^(?i)(?:(?!<USER_NAME>).)+$");

    String password = "USername3.";
    UserData user1Data = new UserData("1", "Username3.");
    ValidationErrors errors = validator.validate(password, user1Data);

    Assertions.assertAll(
      () -> assertTrue(errors.hasErrors()),
      () -> assertThat(errors.getErrorMessages()).containsExactly(rule.getErrMessageId())
    );
  }
}
