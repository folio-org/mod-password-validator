package org.folio.pv.service;

import feign.FeignException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.folio.pv.client.UserClient;
import org.folio.pv.domain.ValidationType;
import org.folio.pv.domain.dto.Password;
import org.folio.pv.domain.dto.PasswordCheck;
import org.folio.pv.domain.dto.UserData;
import org.folio.pv.domain.dto.ValidationResult;
import org.folio.pv.domain.entity.PasswordValidationRule;
import org.folio.pv.service.exception.NoRulesMatchedException;
import org.folio.pv.service.exception.UserNotFoundException;
import org.folio.pv.service.validator.ValidatorRegistry;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Log4j2
public class PasswordValidatorServiceImpl implements PasswordValidatorService {
  public static final String VALIDATION_VALID_RESULT = "valid";
  public static final String VALIDATION_INVALID_RESULT = "invalid";
  private static final Set<String> PASSWORD_CHECK_RULE_NAMES = Set.of("password_length", "alphabetical_letters",
      "numeric_symbol", "special_character", "no_user_name");

  private final ValidationRuleService validationRuleService;
  private final ValidatorRegistry validationRegistry;
  private final UserClient userClient;

  @Override
  public ValidationResult validatePasswordByRules(String tenant, final Password passwordContainer) {
    String userName = getUserNameByUserId(passwordContainer.getUserId());
    UserData userData = new UserData(passwordContainer.getUserId(), userName);
    String password = passwordContainer.getPassword();
    List<PasswordValidationRule> enabledRules = getSortedRules(validationRuleService.getEnabledRules(tenant));
    return validateRules(enabledRules, password, userData, true);
  }

  @Override
  public ValidationResult checkPassword(String tenant, PasswordCheck passwordCheck) {
    String username = passwordCheck.getUsername();
    String password = passwordCheck.getPassword();
    UserData userData = new UserData();
    userData.setName(username);
    List<PasswordValidationRule> rules = getSortedRules(getPasswordCheckRules(tenant));
    return validateRules(rules, password, userData, false);
  }

  private List<PasswordValidationRule> getSortedRules(List<PasswordValidationRule> rules) {
    if (rules == null) {
      return Collections.emptyList();
    }
    return rules.stream()
        .sorted(Comparator.comparing(PasswordValidationRule::getOrderNo))
        .toList();
  }

  private ValidationResult validateRules(List<PasswordValidationRule> rules, String password, UserData userData,
      boolean isStrongValidation) {
    List<String> validationMessages = new ArrayList<>();
    for (PasswordValidationRule rule : rules) {
      log.info("Validating password with rule: {}", ruleBriefDescription(rule));
      var validator = validationRegistry.validatorByRule(rule);
      var errors = validator.validate(password, userData);
      if (!errors.hasErrors()) {
        log.info("Rule passed: {}", ruleBriefDescription(rule));
      } else {
        validationMessages.addAll(errors.getErrorMessages());
        log.warn("Rule failed: {}, errors: {}", ruleBriefDescription(rule),
            String.join(", ", errors.getErrorMessages()));
        if (isStrongValidation && ValidationType.STRONG == rule.getValidationType()) {
          break;
        }
      }
    }
    ValidationResult validationResult = new ValidationResult();
    validationResult.setMessages(validationMessages);
    validationResult.setResult(validationMessages.isEmpty() ? VALIDATION_VALID_RESULT : VALIDATION_INVALID_RESULT);
    log.info("Validation result: {}", validationResult);
    return validationResult;
  }

  private List<PasswordValidationRule> getPasswordCheckRules(String tenant) {
    var rules = validationRuleService.getEnabledRules(tenant).stream()
        .filter(rule -> PASSWORD_CHECK_RULE_NAMES.contains(rule.getName()))
        .collect(Collectors.toList());

    if (rules.isEmpty()) {
      throw new NoRulesMatchedException("No matched rules for password checking");
    }

    return rules;
  }

  private String getUserNameByUserId(String userId) {
    try {
      return userClient.getUserById(userId)
          .map(UserClient.UserDto::getUsername)
          .orElseThrow(() -> new UserNotFoundException(userId));
    } catch (FeignException.NotFound e) {
      log.warn("Failed on getting userName by given id: {}, msg: {}", userId, e.getMessage());
      throw new UserNotFoundException(userId);
    }
  }

  private String ruleBriefDescription(PasswordValidationRule rule) {
    return new ToStringBuilder(rule)
        .append("id", rule.getId())
        .append("name", rule.getName())
        .append("type", rule.getRuleType())
        .append("validationType", rule.getValidationType())
        .build();
  }
}
