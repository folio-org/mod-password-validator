package org.folio.rest.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.folio.HttpStatus;
import org.folio.rest.jaxrs.model.PasswordJson;
import org.folio.rest.jaxrs.model.ValidationTemplateJson;
import org.folio.rest.jaxrs.resource.PasswordResource;
import org.folio.services.validator.engine.ValidationEngineService;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

@RunWith(MockitoJUnitRunner.class)
public class PasswordResourceUnitTest {

  private static final String TENANT_HEADER = "x-okapi-tenant";
  private static final String TENANT_ID = "diku";

  @Mock
  private Context vertxContext;

  @Mock
  private ValidationEngineService validationEngineService;

  @InjectMocks
  private PasswordResource passwordResource = new PasswordResourceImpl();

  @Test
  public void shouldReturnServiceResponseWhenSucceeded() throws Exception {
    String givenPassword = "password";
    PasswordJson requestEntity = new PasswordJson().withPassword(givenPassword);
    Map<String, String> okapiHeaders = new HashMap<>();
    okapiHeaders.put(TENANT_HEADER, TENANT_ID);

    JsonObject mockResponse =
      new JsonObject()
        .put("result", "Valid")
        .put("messages", new JsonArray());
    Mockito.doAnswer(new AsyncResultAnswer<>(Future.succeededFuture(mockResponse), 2))
      .when(validationEngineService)
      .validatePassword(ArgumentMatchers.eq(givenPassword), ArgumentMatchers.eq(okapiHeaders), ArgumentMatchers.any());

    Handler<AsyncResult<Response>> checkingHandler = result -> {
      Response response = result.result();
      Assert.assertEquals(HttpStatus.HTTP_ACCEPTED.toInt(), response.getStatus());
      ValidationTemplateJson responseEntity = (ValidationTemplateJson) response.getEntity();
      ValidationTemplateJson expectedEntity = mockResponse.mapTo(ValidationTemplateJson.class);
      Assert.assertEquals(expectedEntity.getResult(), responseEntity.getResult());
      Assert.assertEquals(expectedEntity.getMessages(), responseEntity.getMessages());
    };
    passwordResource.postPasswordValidate(requestEntity, okapiHeaders, checkingHandler, vertxContext);
    Mockito.verify(validationEngineService)
      .validatePassword(ArgumentMatchers.eq(givenPassword), ArgumentMatchers.eq(okapiHeaders), ArgumentMatchers.any());
  }

  @Test
  public void shouldReturnInternalServerError() throws Exception {
    String givenPassword = "password";
    PasswordJson requestEntity = new PasswordJson().withPassword(givenPassword);
    Map<String, String> okapiHeaders = new HashMap<>();
    okapiHeaders.put(TENANT_HEADER, TENANT_ID);
    String exceptionMessage = "This is an exception";
    Mockito.doAnswer(new AsyncResultAnswer<>(Future.failedFuture(new Exception(exceptionMessage)), 2))
      .when(validationEngineService)
      .validatePassword(ArgumentMatchers.eq(givenPassword), ArgumentMatchers.eq(okapiHeaders), ArgumentMatchers.any());

    Handler<AsyncResult<Response>> checkingHandler = result -> {
      Response response = result.result();
      Assert.assertEquals(HttpStatus.HTTP_INTERNAL_SERVER_ERROR.toInt(), response.getStatus());
      Assert.assertThat((String) response.getEntity(), Matchers.containsString(exceptionMessage));
    };
    passwordResource.postPasswordValidate(requestEntity, okapiHeaders, checkingHandler, vertxContext);
    Mockito.verify(validationEngineService)
      .validatePassword(ArgumentMatchers.eq(givenPassword), ArgumentMatchers.eq(okapiHeaders), ArgumentMatchers.any());
  }
}