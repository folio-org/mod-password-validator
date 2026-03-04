package org.folio.pv.client;

import java.io.IOException;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

public class PwnedClientAddPaddingInterceptor implements ClientHttpRequestInterceptor {

  private static final String ADD_PADDING_HEADER = "Add-Padding";

  private final boolean paddingEnabled;

  public PwnedClientAddPaddingInterceptor(boolean paddingEnabled) {
    this.paddingEnabled = paddingEnabled;
  }

  @Override
  public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
    throws IOException {
    if (paddingEnabled) {
      request.getHeaders().add(ADD_PADDING_HEADER, "true");
    }
    return execution.execute(request, body);
  }
}
