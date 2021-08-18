package org.folio.pv.api;

import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.apache.http.HttpStatus.SC_NOT_FOUND;
import static org.apache.http.HttpStatus.SC_OK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;

import static org.folio.pv.testutils.APITestUtils.PASSWORD_VALIDATE_PATH;
import static org.folio.pv.testutils.APITestUtils.mockGet;
import static org.folio.pv.testutils.APITestUtils.mockPost;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import org.folio.pv.domain.dto.Password;
import org.folio.pv.domain.dto.ValidationResult;

class PasswordValidatorControllerApiTest extends BaseApiTest {

  @Test
  void validateInvalidPassword() {
    mockGet("/users.*", "{\"users\":[{\"username\":\"cedrick\"}],\"totalRecords\":1}", SC_OK,
      APPLICATION_JSON_VALUE, wireMockServer
    );
    var password = new Password().password("test-password").userId(UUID.randomUUID().toString());

    var validationResult = verifyPost(PASSWORD_VALIDATE_PATH, password, SC_OK).as(ValidationResult.class);

    assertThat(validationResult)
      .hasFieldOrPropertyWithValue("result", "invalid");
  }

  @Test
  void validateValidPassword() {
    mockGet("/users.*", "{\"users\":[{\"username\":\"cedrick\"}],\"totalRecords\":1}", SC_OK,
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
    String expectedErrorMessage = String.format("User is not found: id = %s", password.getUserId());

    mockGet("/users.*", "{\"totalRecords\":0}", SC_OK,
      APPLICATION_JSON_VALUE, wireMockServer
    );
    ValidationResult validationResult = verifyPost(PASSWORD_VALIDATE_PATH, password, SC_NOT_FOUND).as(ValidationResult.class);

    assertEquals("invalid", validationResult.getResult());
    assertTrue(validationResult.getMessages().contains(expectedErrorMessage));
  }

  @Test
  void validateInvalid_ifUserIdNotProvided() {
    Password password = new Password().password("test-password");
    String expectedErrorMessage = "User Id is not provided";

    ValidationResult validationResult = verifyPost(PASSWORD_VALIDATE_PATH, password, SC_BAD_REQUEST).as(ValidationResult.class);

    assertEquals("invalid", validationResult.getResult());
    assertTrue(validationResult.getMessages().contains(expectedErrorMessage));
  }

  @Test
  void validateInvalid_ifPasswordNotProvided() {
    Password password = new Password().userId(UUID.randomUUID().toString());
    String expectedErrorMessage = "Password is not provided";

    ValidationResult validationResult = verifyPost(PASSWORD_VALIDATE_PATH, password, SC_BAD_REQUEST).as(ValidationResult.class);

    assertEquals("invalid", validationResult.getResult());
    assertTrue(validationResult.getMessages().contains(expectedErrorMessage));
  }
}
