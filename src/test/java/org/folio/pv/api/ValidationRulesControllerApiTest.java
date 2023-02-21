package org.folio.pv.api;

import static org.apache.http.HttpStatus.SC_OK;
import static org.apache.http.HttpStatus.SC_UNPROCESSABLE_ENTITY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.pv.testutils.ApiTestUtils.LIMIT_PARAM;
import static org.folio.pv.testutils.ApiTestUtils.QUERY_PARAM;
import static org.folio.pv.testutils.ApiTestUtils.rulePath;
import static org.folio.pv.testutils.ApiTestUtils.rulesPath;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.List;
import java.util.UUID;
import org.folio.pv.domain.RuleState;
import org.folio.pv.domain.RuleType;
import org.folio.pv.domain.ValidationType;
import org.folio.pv.domain.dto.Error;
import org.folio.pv.domain.dto.ValidationRule;
import org.folio.pv.domain.dto.ValidationRuleCollection;
import org.folio.spring.test.type.IntegrationTest;
import org.junit.jupiter.api.Test;

@IntegrationTest
class ValidationRulesControllerApiTest extends BaseApiTest {
  private static final String RULE_ID = "5105b55a-b9a3-4f76-9402-a5243ea63c95";
  private static final String ERR_MSG_ID = "password.test.invalid";
  private static final String RULE_NAME = "test rule";
  private static final String MODULE_NAME = "test";
  private static final String REG_EXP = "\\w+";
  private static final int ORDER_NUMBER = 12;

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
    //  GIVEN
    ValidationRule newRule = new ValidationRule()
      .name(RULE_NAME)
      .type(ValidationRule.TypeEnum.REGEXP)
      .validationType(ValidationRule.ValidationTypeEnum.SOFT)
      .state(ValidationRule.StateEnum.DISABLED)
      .moduleName(MODULE_NAME)
      .orderNo(ORDER_NUMBER)
      .errMessageId(ERR_MSG_ID)
      .expression(REG_EXP);

    //  WHEN
    var createdRuleId = verifyPost(rulesPath(), newRule, SC_OK).as(ValidationRule.class).getId();
    var actualRule = dbTestUtils.getValidationRuleById(UUID.fromString(createdRuleId));

    //  THEN
    assertThat(actualRule)
      .hasFieldOrPropertyWithValue("id", UUID.fromString(createdRuleId))
      .hasFieldOrPropertyWithValue("name", RULE_NAME)
      .hasFieldOrPropertyWithValue("ruleType", RuleType.REGEXP)
      .hasFieldOrPropertyWithValue("validationType", ValidationType.SOFT)
      .hasFieldOrPropertyWithValue("ruleState", RuleState.DISABLED)
      .hasFieldOrPropertyWithValue("orderNo", ORDER_NUMBER)
      .hasFieldOrPropertyWithValue("ruleExpression", REG_EXP)
      .hasFieldOrPropertyWithValue("errMessageId", ERR_MSG_ID);
  }


  @Test
  void testPutValidationRule() {
    //  GIVEN
    var rule = verifyGet(rulePath(RULE_ID), SC_OK).as(ValidationRule.class);
    rule.state(ValidationRule.StateEnum.DISABLED);

    //  WHEN
    verifyPut(rulesPath(), rule, SC_OK).as(ValidationRule.class);
    var actualRule = dbTestUtils.getValidationRuleById(UUID.fromString(RULE_ID));

    //  THEN
    assertThat(actualRule)
      .hasFieldOrPropertyWithValue("id", UUID.fromString(RULE_ID))
      .hasFieldOrPropertyWithValue("ruleState", RuleState.DISABLED);
  }
}
