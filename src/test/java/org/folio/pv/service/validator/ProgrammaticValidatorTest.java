package org.folio.pv.service.validator;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.google.common.net.HttpHeaders.CONTENT_TYPE;
import static java.util.Collections.singleton;
import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.pv.testutils.RandomTestData.nextRandomRuleOfType;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import io.github.glytching.junit.extension.random.Random;
import io.github.glytching.junit.extension.random.RandomBeansExtension;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.IntStream;
import org.folio.pv.domain.RuleType;
import org.folio.pv.domain.ValidationType;
import org.folio.pv.domain.dto.Password;
import org.folio.pv.domain.dto.UserData;
import org.folio.pv.domain.dto.ValidationErrors;
import org.folio.pv.domain.entity.PasswordValidationRule;
import org.folio.spring.DefaultFolioExecutionContext;
import org.folio.spring.FolioExecutionContext;
import org.folio.spring.FolioModuleMetadata;
import org.folio.spring.config.FolioSpringConfiguration;
import org.folio.spring.integration.XOkapiHeaders;
import org.folio.spring.liquibase.FolioSpringLiquibase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

@ExtendWith({
  MockitoExtension.class,
  RandomBeansExtension.class
})
@SpringBootTest
class ProgrammaticValidatorTest {

  private static final String EXTERNAL_SERVICE_PATH = "/service";
  private static final String TEST_TENANT = "test_tenant";

  @Autowired
  private WireMockServer service;

  @Autowired
  private ObjectMapper jacksonMapper;
  @Autowired
  private FolioExecutionContext folioExecutionContext;

  private PasswordValidationRule rule;
  private ProgrammaticValidator validator;

  @Random
  private String password;
  @Random
  private UserData userData;

  static IntStream failedStatuses() {
    return IntStream.range(400, 511);
  }

  private static Function<MappingBuilder, MappingBuilder> successfulResponse(int status) {
    return response -> response.willReturn(
      aResponse()
        .withStatus(status)
        .withHeader(CONTENT_TYPE, "application/json")
        .withBody("{ \"result\": \"success\" }")
    );
  }

  private static Function<MappingBuilder, MappingBuilder> successfulResponseWithErrors(int status,
                                                                                       String message) {
    return response -> response.willReturn(
      aResponse()
        .withStatus(status)
        .withHeader(CONTENT_TYPE, "application/json")
        .withBody("{"
          + " \"result\": \"success\","
          + " \"messages\": [ \"" + message + "\" ]"
          + " }")
    );
  }

  private static Function<MappingBuilder, MappingBuilder> serverErrorResponse(int status, String message) {
    return response -> response.willReturn(aResponse().withStatus(status).withBody(message));
  }

  @BeforeEach
  void setUp() {
    rule = nextRandomRuleOfType(RuleType.PROGRAMMATIC);
    rule.setImplementationReference(EXTERNAL_SERVICE_PATH);

    validator = new ProgrammaticValidator(rule, folioExecutionContext, jacksonMapper);
  }

  @AfterEach
  void tearDown() {
    service.resetToDefaultMappings();
  }

  @ParameterizedTest
  @ValueSource(ints = {200, 201, 202})
  void shouldPassIfServiceReturnsSuccessfulResponse(int responseStatus) throws JsonProcessingException {
    stubPostWithResponse(successfulResponse(responseStatus));

    ValidationErrors errors = validator.validate(password, userData);

    assertFalse(errors.hasErrors());
    verifyPostRequest();
  }

  @ParameterizedTest
  @ValueSource(ints = {200, 201, 202})
  void shouldFailIfServiceReturnsSuccessfulResponseWithErrors(int responseStatus) throws JsonProcessingException {
    stubPostWithResponse(successfulResponseWithErrors(responseStatus, "Invalid password"));

    ValidationErrors errors = validator.validate(password, userData);

    Assertions.assertAll(
      () -> assertTrue(errors.hasErrors()),
      () -> assertThat(errors.getErrorMessages()).containsExactly("Invalid password")
    );
    verifyPostRequest();
  }

  @ParameterizedTest
  @MethodSource("failedStatuses")
  void shouldFailWithRuntimeExcIfValidationIsStrongAndServiceReturnsFailure(int responseStatus)
    throws JsonProcessingException {

    stubPostWithResponse(serverErrorResponse(responseStatus, "Server error"));
    rule.setValidationType(ValidationType.STRONG);

    Exception exc = Assertions.assertThrows(RuntimeException.class,
      () -> validator.validate(password, userData));

    assertEquals("Server error", exc.getMessage());
    verifyPostRequest();
  }

  @ParameterizedTest
  @MethodSource("failedStatuses")
  void shouldPassIfValidationIsSoftAndServiceReturnsFailure(int responseStatus)
    throws JsonProcessingException {

    stubPostWithResponse(serverErrorResponse(responseStatus, "Server error"));
    rule.setValidationType(ValidationType.SOFT);

    ValidationErrors errors = validator.validate(password, userData);

    assertFalse(errors.hasErrors());
    verifyPostRequest();
  }

  private void stubPostWithResponse(Function<MappingBuilder, MappingBuilder> responseBuilder) {
    service.stubFor(responseBuilder.apply(post(urlEqualTo(EXTERNAL_SERVICE_PATH))));
  }

  private void verifyPostRequest() throws JsonProcessingException {
    service.verify(
      postRequestedFor(urlEqualTo(EXTERNAL_SERVICE_PATH))
        .withHeader("Content-Type", equalTo("application/json"))
        .withHeader(XOkapiHeaders.URL, equalTo(service.baseUrl()))
        .withHeader(XOkapiHeaders.TENANT, equalTo(TEST_TENANT))
        .withRequestBody(equalToJson(jacksonMapper.writeValueAsString(
          new Password().password(password).userId(userData.getId())
        )))
    );
  }

  @Configuration
  @Import({
    FolioSpringConfiguration.class,
    JacksonAutoConfiguration.class,
    FeignAutoConfiguration.class
  })
  public static class Config {

    @MockBean
    private FolioSpringLiquibase liquibase;
    @MockBean
    private JdbcTemplate jdbcTemplate;

    @Bean
    public FolioExecutionContext folioExecutionContext(FolioModuleMetadata moduleMetadata,
                                                       WireMockServer wireMockServer) {
      Map<String, Collection<String>> headers = new HashMap<>();
      headers.put(XOkapiHeaders.URL, singleton(wireMockServer.baseUrl()));
      headers.put(XOkapiHeaders.TENANT, singleton(TEST_TENANT));

      return new DefaultFolioExecutionContext(moduleMetadata, headers);
    }

    @Bean
    public WireMockServer wireMockServer() {
      WireMockServer wireMockServer = new WireMockServer(new WireMockConfiguration().dynamicPort());
      wireMockServer.start();
      return wireMockServer;
    }

  }

}
