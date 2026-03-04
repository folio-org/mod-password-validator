package org.folio.pv.config;

import org.folio.pv.client.HashedPasswordUsageCollectionConverter;
import org.folio.pv.client.PwnedClient;
import org.folio.pv.client.PwnedClientAddPaddingInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class PwnedClientConfiguration {

  @Bean
  public PwnedClient pwnedClient(
    @Value("${pwned-passwords.client.url}") String pwnedPasswordsUrl,
    @Value("${pwned-passwords.padding.enabled}") boolean paddingEnabled) {

    var restTemplate = new RestTemplate();
    restTemplate.getMessageConverters().addFirst(new HashedPasswordUsageCollectionConverter<>());

    var restClient = RestClient.builder(restTemplate)
      .baseUrl(pwnedPasswordsUrl)
      .requestInterceptor(new PwnedClientAddPaddingInterceptor(paddingEnabled))
      .build();

    return HttpServiceProxyFactory
      .builderFor(RestClientAdapter.create(restClient))
      .build()
      .createClient(PwnedClient.class);
  }

}
