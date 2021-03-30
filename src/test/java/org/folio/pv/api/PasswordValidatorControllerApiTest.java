package org.folio.pv.api;

import static org.apache.http.HttpStatus.SC_OK;
import static org.assertj.core.api.Assertions.assertThat;

import static org.folio.pv.testutils.APITestUtils.PASSWORD_VALIDATE_PATH;
import static org.folio.pv.testutils.APITestUtils.mockGet;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import org.folio.pv.domain.dto.Password;
import org.folio.pv.domain.dto.ValidationResult;

class PasswordValidatorControllerApiTest extends BaseApiTest {

  @Test
  void validatePassword() {
    mockGet("/users.*", "{\"users\":[{\"username\":\"cedrick\"}],\"totalRecords\":1}", SC_OK, wireMockServer);
    var password = new Password().password("test-password").userId(UUID.randomUUID().toString());

    var validationResult = verifyPost(PASSWORD_VALIDATE_PATH, password, SC_OK).as(ValidationResult.class);

    assertThat(validationResult)
      .hasFieldOrPropertyWithValue("result", "invalid");
  }
}