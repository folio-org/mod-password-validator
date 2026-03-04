package org.folio.pv.client;

import java.util.List;
import org.folio.pv.domain.dto.HashedPasswordUsage;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange(accept = MediaType.TEXT_PLAIN_VALUE)
public interface PwnedClient {

  @GetExchange(value = "/range/{hashPrefix}")
  List<HashedPasswordUsage> getPwdRange(@PathVariable String hashPrefix);

}
