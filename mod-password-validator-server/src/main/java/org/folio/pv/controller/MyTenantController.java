package org.folio.pv.controller;

import lombok.extern.slf4j.Slf4j;
import org.folio.spring.FolioExecutionContextService;
import org.folio.spring.liquibase.FolioSpringLiquibase;
import org.folio.tenant.domain.dto.TenantAttributes;
import org.folio.tenant.rest.resources.TenantApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import javax.validation.Valid;

// an example how to override tenant controller
// tne name of the bean MUST be folioTenantController see org.folio.spring.controller.TenantController for details
@Slf4j
//@RestController("folioTenantController")
//@RequestMapping(value = "/_/")
public class MyTenantController implements TenantApi {

  private final FolioSpringLiquibase folioSpringLiquibase;

  private final FolioExecutionContextService contextService;

  @Autowired
  public MyTenantController(FolioSpringLiquibase folioSpringLiquibase,
                            FolioExecutionContextService contextService) {
    this.folioSpringLiquibase = folioSpringLiquibase;
    this.contextService = contextService;
  }

  @Override
  public ResponseEntity<String> postTenant(@Valid TenantAttributes tenantAttributes) {
    return null;
  }
}
