package org.folio.pv.api;

import static io.zonky.test.db.AutoConfigureEmbeddedDatabase.DatabaseProvider.ZONKY;
import static io.zonky.test.db.AutoConfigureEmbeddedDatabase.DatabaseType.POSTGRES;
import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.pv.testutils.ApiTestUtils.TENANT_ID;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.restassured.RestAssured;
import io.restassured.http.Header;
import io.restassured.response.Response;
import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.folio.pv.service.ValidationRuleServiceImplTest;
import org.folio.pv.service.validator.ProgrammaticValidatorTest;
import org.folio.pv.testutils.extension.WireMockInitializer;
import org.folio.spring.FolioModuleMetadata;
import org.folio.spring.integration.XOkapiHeaders;
import org.folio.tenant.domain.dto.TenantAttributes;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;


@ActiveProfiles("test")
@AutoConfigureEmbeddedDatabase(beanName = "dataSource", type = POSTGRES, provider = ZONKY)
@ContextConfiguration(initializers = {WireMockInitializer.class})
@EnableAutoConfiguration(exclude = {FlywayAutoConfiguration.class})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
  properties = {"folio.jpa.repository.base-packages=org.folio.pv"})
class BaseApiTest {

  private static boolean dbInitialized = false;

  @Autowired
  protected WireMockServer wireMockServer;
  @Autowired
  protected FolioModuleMetadata metadata;
  @Autowired
  protected JdbcTemplate jdbcTemplate;

  @Value("${x-okapi-url}")
  private String okapiUrl;
  @LocalServerPort
  private Integer port;

  @BeforeEach
  void before() {
    if (!dbInitialized) {
      verifyPost("/_/tenant", new TenantAttributes().moduleTo("mod-password-validator"), HttpStatus.SC_NO_CONTENT);
      dbInitialized = true;
    }
  }

  @AfterEach
  void afterEach() {
    this.wireMockServer.resetAll();
  }

  @Test
  void contextLoads() {
    assertThat(metadata).isNotNull();
  }

  protected Response verifyGet(String path, int code) {
    return RestAssured.with()
      .header(new Header(XOkapiHeaders.URL, okapiUrl))
      .header(new Header(XOkapiHeaders.TENANT, TENANT_ID))
      .get(getRequestUrl(path))
      .then()
      .statusCode(code)
      .contentType(MediaType.APPLICATION_JSON_VALUE)
      .extract()
      .response();
  }

  protected Response verifyPut(String path, Object body, int code) {
    return RestAssured.with()
      .header(new Header(XOkapiHeaders.URL, okapiUrl))
      .header(new Header(XOkapiHeaders.TENANT, TENANT_ID))
      .body(body)
      .contentType(MediaType.APPLICATION_JSON_VALUE)
      .put(getRequestUrl(path))
      .then()
      .statusCode(code)
      .contentType(StringUtils.EMPTY)
      .extract()
      .response();
  }

  protected Response verifyPost(String path, Object body, int code) {
    return RestAssured.with()
      .header(new Header(XOkapiHeaders.URL, okapiUrl))
      .header(new Header(XOkapiHeaders.TENANT, TENANT_ID))
      .body(body)
      .contentType(MediaType.APPLICATION_JSON_VALUE)
      .post(getRequestUrl(path))
      .then()
      .statusCode(code)
      .contentType(StringUtils.EMPTY)
      .extract()
      .response();
  }

  private String getRequestUrl(String path) {
    return "http://localhost:" + port + path;
  }


  @Configuration
  @ComponentScan(basePackages = {"org.folio.pv"},
    excludeFilters = {@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
      value = {ValidationRuleServiceImplTest.Config.class, ProgrammaticValidatorTest.Config.class})})
  static class TestConfiguration {
    @Bean
    public Object liquibaseDatabaseExtension() {
      return new Object();
    }
  }
}
