package org.folio.pv.service;

import feign.FeignException;
import java.util.ArrayList;
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
    var userName = getUserNameByUserId(passwordContainer.getUserId());
    var userData = new UserData(passwordContainer.getUserId(), userName);
    var password = passwordContainer.getPassword();
    var enabledRules = validationRuleService.getEnabledRules(tenant);
    enabledRules.sort(Comparator.comparing(PasswordValidationRule::getOrderNo));
    return validate(enabledRules, password, userData);
  }

  @Override
  public ValidationResult checkPassword(String tenant, PasswordCheck passwordCheck) {
    var username = passwordCheck.getUsername();
    var password = passwordCheck.getPassword();
    var userData = new UserData();
    userData.setName(username);
    var rules = getPasswordCheckRules(tenant);
    return validate(rules, password, userData);
  }

  private ValidationResult validate(List<PasswordValidationRule> rules, String password, UserData userData) {
    rules.sort(Comparator.comparing(PasswordValidationRule::getOrderNo));
    List<String> validationMessages = new ArrayList<>();
    for (PasswordValidationRule rule : rules) {
      var validator = validationRegistry.validatorByRule(rule);
      log.info("Validating password with rule: {}", ruleBriefDescription(rule));
      var errors = validator.validate(password, userData);
      if (!errors.hasErrors()) {
        log.info("validatePasswordByRules:: No validation errors");
      }
      validationMessages.addAll(errors.getErrorMessages());
      if (errors.hasErrors() && ValidationType.STRONG == rule.getValidationType()) {
        log.warn("Failed on password validating, error msg: {}", String.join(", ", validationMessages));
        break;
      }
    }
    var validationResult = new ValidationResult();
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
