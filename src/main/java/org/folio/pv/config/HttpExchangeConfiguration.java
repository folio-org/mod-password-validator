package org.folio.pv.config;

import org.folio.pv.client.UserClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class HttpExchangeConfiguration {

  @Bean
  public UserClient userClient(HttpServiceProxyFactory factory) {
    return factory.createClient(UserClient.class);
  }

}
