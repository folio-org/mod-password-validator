package org.folio.pv.client;

import java.util.Optional;
import org.folio.pv.client.model.User;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange(url = "users", accept = MediaType.APPLICATION_JSON_VALUE)
public interface UserClient {

  @GetExchange(value = "/{id}")
  Optional<User> getUserById(@PathVariable("id") String id);
}
