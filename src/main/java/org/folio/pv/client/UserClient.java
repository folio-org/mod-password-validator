package org.folio.pv.client;

import java.util.Optional;

import lombok.Value;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient("users")
public interface UserClient {

  @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  Optional<UserDto> getUserById(@PathVariable("id") String id);

  @Value
  class UserDto {

    String id;
    String username;
  }
}
