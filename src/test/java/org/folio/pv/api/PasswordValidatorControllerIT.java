package org.folio.pv.api;

import static org.apache.http.HttpStatus.SC_NOT_FOUND;
import static org.apache.http.HttpStatus.SC_OK;
import static org.apache.http.HttpStatus.SC_UNPROCESSABLE_ENTITY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.pv.testutils.ApiTestUtils.PASSWORD_CHECK_PATH;
import static org.folio.pv.testutils.ApiTestUtils.PASSWORD_VALIDATE_PATH;
import static org.folio.pv.testutils.ApiTestUtils.mockGet;
import static org.folio.pv.testutils.ApiTestUtils.mockPost;
import static org.folio.pv.testutils.ApiTestUtils.rulePath;
import static org.folio.pv.testutils.ApiTestUtils.rulesPath;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;

import java.util.UUID;
import org.folio.pv.domain.dto.Error;
import org.folio.pv.domain.dto.Errors;
import org.folio.pv.domain.dto.Password;
import org.folio.pv.domain.dto.PasswordCheck;
import org.folio.pv.domain.dto.ValidationResult;
import org.folio.pv.domain.dto.ValidationRule;
import org.folio.pv.domain.dto.ValidationRule.StateEnum;
import org.folio.spring.testing.type.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@IntegrationTest
class PasswordValidatorControllerIT extends BaseApiTest {

  private static final String WHITE_SPACE_RULE_ID = "51e201ba-95d3-44e5-b4ec-f0059f11afcb";

  @Test
  void validateInvalidPassword() {
    mockGet("/users.*", "{\"username\":\"cedrick\"}", SC_OK,
      APPLICATION_JSON_VALUE, wireMockServer
    );
    var password = new Password().password("test-password").userId(UUID.randomUUID().toString());

    var validationResult = verifyPost(PASSWORD_VALIDATE_PATH, password, SC_OK).as(ValidationResult.class);

    assertThat(validationResult)
      .hasFieldOrPropertyWithValue("result", "invalid");
  }

  @Test
  void validateValidPassword() {
    mockGet("/users.*", "{\"username\":\"cedrick\"}", SC_OK,
      APPLICATION_JSON_VALUE, wireMockServer
    );
    mockPost("/authn/password/repeatable", "{\"result\":\"valid\"}", SC_OK, wireMockServer);
    mockGet("/range/.*", "0018A45C4D1DEF81644B54AB7F969B88D65:0", SC_OK, TEXT_PLAIN_VALUE, wireMockServer);
    var password = new Password().password("7Xu^&t[:J3Hha(<B").userId(UUID.randomUUID().toString());

    var validationResult = verifyPost(PASSWORD_VALIDATE_PATH, password, SC_OK).as(ValidationResult.class);

    assertThat(validationResult)
      .hasFieldOrPropertyWithValue("result", "valid");
  }

  @Test
  void validateInvalid_ifUserNotFound() {
    Password password = new Password().password("test-password").userId(UUID.randomUUID().toString());
    String expectedErrorMessage = "User with given id not found";

    mockGet("/users.*", expectedErrorMessage, SC_NOT_FOUND,
      APPLICATION_JSON_VALUE, wireMockServer
    );
    Error error = verifyPost(PASSWORD_VALIDATE_PATH, password, SC_NOT_FOUND).as(Error.class);

    assertEquals(expectedErrorMessage, error.getMessage());
  }

  @Test
  void validateInvalid_ifUserIdNotProvided() {
    Password password = new Password().password("test-password");
    Errors errors = verifyPost(PASSWORD_VALIDATE_PATH, password, SC_UNPROCESSABLE_ENTITY).as(Errors.class);
    assertErrorField(errors, "userId");
  }

  @Test
  void validateInvalid_ifPasswordNotProvided() {
    Password password = new Password().userId(UUID.randomUUID().toString());
    Errors errors = verifyPost(PASSWORD_VALIDATE_PATH, password, SC_UNPROCESSABLE_ENTITY).as(Errors.class);
    assertErrorField(errors, "password");
  }

  @Test
  void checkPassword_success() {
    mockGet("/range/.*", "0018A45C4D1DEF81644B54AB7F969B88D65:0", SC_OK, TEXT_PLAIN_VALUE, wireMockServer);
    var password = new PasswordCheck().password("7Xu^&t[:J3Hha(<B").username("testUser");

    var validationResult = verifyPost(PASSWORD_CHECK_PATH, password, SC_OK).as(ValidationResult.class);

    assertThat(validationResult).hasFieldOrPropertyWithValue("result", "valid");
  }

  @ParameterizedTest
  @ValueSource(strings = {"test", "TEST1234", "test@132", "testUser", "someUser123", "testUser123@", "1234443332",
      "7Xu^&t[:J3Hha(<B ", " 7Xu^&t[:J3Hha(<B"})
  void checkPassword_fail_whenInvalidPassword(String invalidPassword) {
    updateRuleState(WHITE_SPACE_RULE_ID, StateEnum.ENABLED);
    mockGet("/range/.*", "0018A45C4D1DEF81644B54AB7F969B88D65:0", SC_OK, TEXT_PLAIN_VALUE, wireMockServer);
    var password = new PasswordCheck().password(invalidPassword).username("testUser");

    var validationResult = verifyPost(PASSWORD_CHECK_PATH, password, SC_OK).as(ValidationResult.class);

    assertThat(validationResult).hasFieldOrPropertyWithValue("result", "invalid");
    updateRuleState(WHITE_SPACE_RULE_ID, StateEnum.DISABLED);
  }

  @Test
  void checkPassword_fail_whenEmptyUsername() {
    mockGet("/range/.*", "0018A45C4D1DEF81644B54AB7F969B88D65:0", SC_OK, TEXT_PLAIN_VALUE, wireMockServer);
    var password = new PasswordCheck().password("7Xu^&t[:J3Hha(<B").username("");

    var validationResult = verifyPost(PASSWORD_CHECK_PATH, password, SC_OK).as(ValidationResult.class);

    assertThat(validationResult).hasFieldOrPropertyWithValue("result", "invalid");
    assertEquals("password.usernameDuplicate.invalid", validationResult.getMessages().getFirst());
  }

  @Test
  void checkPassword_fail_whenNullUsername() {
    var password = new PasswordCheck().password("7Xu^&t[:J3Hha(<B");

    Errors errors = verifyPost(PASSWORD_CHECK_PATH, password, SC_UNPROCESSABLE_ENTITY).as(Errors.class);

    assertErrorField(errors, "username");
  }

  @Test
  void checkPassword_fail_whenEmptyPassword() {
    var password = new PasswordCheck().password("").username("testUser");

    Errors errors = verifyPost(PASSWORD_CHECK_PATH, password, SC_UNPROCESSABLE_ENTITY).as(Errors.class);

    assertErrorField(errors, "password");
  }

  private void assertErrorField(Errors errors, String expectedErrorField) {
    assertEquals(expectedErrorField, errors.getErrors().getFirst().getParameters().getFirst().getKey());
  }

  private void updateRuleState(String id, ValidationRule.StateEnum state) {
    var rule = verifyGet(rulePath(id), SC_OK).as(ValidationRule.class);
    rule.state(state);

    verifyPut(rulesPath(), rule, SC_OK).as(ValidationRule.class);
  }
}
