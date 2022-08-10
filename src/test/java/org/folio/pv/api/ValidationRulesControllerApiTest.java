package org.folio.pv.api;


import static org.apache.http.HttpStatus.SC_OK;
import static org.apache.http.HttpStatus.SC_UNPROCESSABLE_ENTITY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.pv.testutils.ApiTestUtils.LIMIT_PARAM;
import static org.folio.pv.testutils.ApiTestUtils.QUERY_PARAM;
import static org.folio.pv.testutils.ApiTestUtils.rulePath;
import static org.folio.pv.testutils.ApiTestUtils.rulesPath;
import static org.folio.pv.testutils.DbTestUtils.getValidationRuleById;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.List;
import java.util.UUID;
import org.folio.pv.domain.dto.Error;
import org.folio.pv.domain.dto.ValidationRule;
import org.folio.pv.domain.dto.ValidationRuleCollection;
import org.junit.jupiter.api.Test;

class ValidationRulesControllerApiTest extends BaseApiTest {

  @Test
  void testGetValidationRuleCollection() {
    var ruleCollection = verifyGet(rulesPath(LIMIT_PARAM, "100"), SC_OK).as(ValidationRuleCollection.class);

    assertThat(ruleCollection)
      .hasFieldOrPropertyWithValue("totalRecords", 11)
      .extracting(ValidationRuleCollection::getRules)
      .satisfies(validationRules -> assertThat(validationRules)
        .hasSize(11)
        .flatExtracting(ValidationRule::getName)
        .containsAll(List.of(
          "no_consecutive_whitespaces",
          "no_repeatable_password",
          "special_character",
          "no_user_name",
          "numeric_symbol",
          "password_length",
          "no_white_space_character",
          "keyboard_sequence",
          "repeating_characters",
          "alphabetical_letters",
          "not_compromised"
        )));
  }

  @Test
  void shouldReturn422onInvalidCql() {
    var error = verifyGet(
      rulesPath(LIMIT_PARAM, "100", QUERY_PARAM, "invalid"),
      SC_UNPROCESSABLE_ENTITY
    ).as(Error.class);

    assertFalse(error.getMessage().isEmpty());
  }

  @Test
  void testGetValidationRuleById() {
    var ruleId = "5105b55a-b9a3-4f76-9402-a5243ea63c95";
    var rule = verifyGet(rulePath(ruleId), SC_OK).as(ValidationRule.class);

    assertThat(rule)
      .extracting(ValidationRule::getId)
      .isEqualTo(ruleId);
  }

  @Test
  void testPostValidationRule() {
    var newRuleName = "test rule";
    var errMessageId = "password.test.invalid";
    var expression = "\\w+";
    var orderNo = 12;
    var newRule = new ValidationRule()
      .name(newRuleName)
      .type(ValidationRule.TypeEnum.REGEXP)
      .validationType(ValidationRule.ValidationTypeEnum.SOFT)
      .state(ValidationRule.StateEnum.DISABLED)
      .moduleName("test")
      .orderNo(orderNo)
      .errMessageId(errMessageId)
      .expression(expression);

    var createdRuleId = verifyPost(rulesPath(), newRule, SC_OK).as(ValidationRule.class).getId();

    var actualRule = getValidationRuleById(UUID.fromString(createdRuleId), metadata, jdbcTemplate);
    assertThat(actualRule)
      .hasFieldOrPropertyWithValue("id", UUID.fromString(createdRuleId))
      .hasFieldOrPropertyWithValue("name", newRuleName)
      .hasFieldOrPropertyWithValue("ruleType", ValidationRule.TypeEnum.REGEXP.getValue())
      .hasFieldOrPropertyWithValue("validationType", ValidationRule.ValidationTypeEnum.SOFT.getValue())
      .hasFieldOrPropertyWithValue("ruleState", ValidationRule.StateEnum.DISABLED.getValue())
      .hasFieldOrPropertyWithValue("orderNo", orderNo)
      .hasFieldOrPropertyWithValue("ruleExpression", expression)
      .hasFieldOrPropertyWithValue("errMessageId", errMessageId);
  }

  @Test
  void testPutValidationRule() {
    var ruleId = "5105b55a-b9a3-4f76-9402-a5243ea63c95";
    var rule = verifyGet(rulePath(ruleId), SC_OK).as(ValidationRule.class);
    rule.state(ValidationRule.StateEnum.DISABLED);

    verifyPut(rulesPath(), rule, SC_OK).as(ValidationRule.class);

    var actualRule = getValidationRuleById(UUID.fromString(ruleId), metadata, jdbcTemplate);
    assertThat(actualRule)
      .hasFieldOrPropertyWithValue("id", UUID.fromString(ruleId))
      .hasFieldOrPropertyWithValue("ruleState", ValidationRule.StateEnum.DISABLED.getValue());
  }
}
