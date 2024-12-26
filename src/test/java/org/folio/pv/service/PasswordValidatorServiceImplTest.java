package org.folio.pv.service;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.pv.service.PasswordValidatorServiceImpl.VALIDATION_INVALID_RESULT;
import static org.folio.pv.service.PasswordValidatorServiceImpl.VALIDATION_VALID_RESULT;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import io.github.glytching.junit.extension.random.Random;
import io.github.glytching.junit.extension.random.RandomBeansExtension;
import java.util.Optional;
import java.util.UUID;
import org.folio.pv.client.UserClient;
import org.folio.pv.domain.ValidationType;
import org.folio.pv.domain.dto.Password;
import org.folio.pv.domain.dto.PasswordCheck;
import org.folio.pv.domain.dto.ValidationErrors;
import org.folio.pv.domain.dto.ValidationResult;
import org.folio.pv.domain.entity.PasswordValidationRule;
import org.folio.pv.service.exception.NoRulesMatchedException;
import org.folio.pv.service.exception.UserNotFoundException;
import org.folio.pv.service.validator.RegExpValidator;
import org.folio.pv.service.validator.ValidatorRegistry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith({MockitoExtension.class, RandomBeansExtension.class})
class PasswordValidatorServiceImplTest {
  private static final String INVALID_PASSWORD = "password.invalid";

  private final ValidationResult valid = new ValidationResult()
      .result(VALIDATION_VALID_RESULT)
      .messages(emptyList());
  private final ValidationResult invalid = new ValidationResult()
      .result(VALIDATION_INVALID_RESULT)
      .messages(singletonList(INVALID_PASSWORD));

  @Mock
  private ValidationRuleService validationRuleService;
  @Mock
  private RegExpValidator validator;
  @Mock
  private ValidatorRegistry validationRegistry;
  @Mock
  private UserClient userClient;
  @InjectMocks
  private PasswordValidatorServiceImpl service;

  @Test
  void shouldFailIfUserNotFoundById(@Random String tenant, @Random Password password) {
    String userId = password.getUserId();
    when(userClient.getUserById(contains(userId))).thenReturn(Optional.empty());

    UserNotFoundException exc = Assertions.assertThrows(UserNotFoundException.class,
        () -> service.validatePasswordByRules(tenant, password));

    assertAll(
        () -> assertThat(exc.getMessage()).containsIgnoringCase("not found"),
        () -> assertEquals(userId, exc.getUserId()));
  }

  @Test
  void validatePasswordByRules_shouldSucceed(@Random String tenant, @Random Password password, @Random String userName,
      @Random PasswordValidationRule enabledRule) {
    String userId = password.getUserId();
    mockFindUserById(userId, userName);
    mockValidatorByRule(tenant, enabledRule);
    ValidationErrors errors = ValidationErrors.none();
    mockValidator(password, userName, errors);

    ValidationResult result = service.validatePasswordByRules(tenant, password);

    assertEquals(valid, result);
  }

  @Test
  void validatePasswordByRules_shouldFailWithValidatorMsg(@Random String tenant, @Random Password password,
      @Random String userName, @Random PasswordValidationRule enabledRule) {
    String userId = password.getUserId();
    mockFindUserById(userId, userName);
    enabledRule.setValidationType(ValidationType.STRONG);
    mockValidatorByRule(tenant, enabledRule);
    ValidationErrors errors = ValidationErrors.of(INVALID_PASSWORD);
    mockValidator(password, userName, errors);
    ValidationResult result = service.validatePasswordByRules(tenant, password);

    assertEquals(invalid, result);
  }

  @ParameterizedTest
  @ValueSource(strings = {"password_length", "alphabetical_letters", "numeric_symbol", "special_character",
      "no_user_name"}
  )
  void checkPassword_shouldSucceed(String ruleName, @Random PasswordCheck passwordCheck,
      @Random PasswordValidationRule enabledRule) {
    String tenant = UUID.randomUUID().toString();
    enabledRule.setName(ruleName);
    mockValidatorByRule(tenant, enabledRule);
    ValidationErrors errors = ValidationErrors.none();
    mockValidator(passwordCheck, errors);

    ValidationResult result = service.checkPassword(tenant, passwordCheck);

    assertEquals(valid, result);
  }

  @ParameterizedTest
  @ValueSource(strings = {"no_repeatable_password", "keyboard_sequence", "no_white_space_character",
      "repeating_characters"}
  )
  void checkPassword_shouldFailWithValidatorMsg(String ruleName, @Random PasswordCheck passwordCheck,
      @Random PasswordValidationRule enabledRule) {
    String tenant = UUID.randomUUID().toString();
    enabledRule.setName(ruleName);
    enabledRule.setValidationType(ValidationType.STRONG);
    when(validationRuleService.getEnabledRules(tenant)).thenReturn(singletonList(enabledRule));

    NoRulesMatchedException exception = Assertions.assertThrows(NoRulesMatchedException.class,
        () -> service.checkPassword(tenant, passwordCheck));

    assertThat(exception.getMessage()).containsIgnoringCase("No matched rules");
  }

  private void mockFindUserById(String userId, String userName) {
    when(userClient.getUserById(contains(userId))).thenReturn(Optional.of(new UserClient.UserDto(userId, userName)));
  }

  private void mockValidator(PasswordCheck passwordCheck, ValidationErrors errors) {
    when(validator.validate(eq(passwordCheck.getPassword()),
        argThat(userData -> userData.getName().equals(passwordCheck.getUsername())
    ))).thenReturn(errors);
  }

  private void mockValidator(Password password, String userName, ValidationErrors errors) {
    when(validator.validate(eq(password.getPassword()), argThat(userData ->
        userData.getId().equals(password.getUserId()) && userData.getName().equals(userName)
    ))).thenReturn(errors);
  }

  private void mockValidatorByRule(String tenant, PasswordValidationRule enabledRule) {
    when(validationRuleService.getEnabledRules(tenant)).thenReturn(singletonList(enabledRule));
    when(validationRegistry.validatorByRule(enabledRule)).thenReturn(validator);
  }
}
