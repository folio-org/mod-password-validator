package org.folio.spring.foliocontext;

import lombok.extern.slf4j.Slf4j;
import org.folio.spring.FolioExecutionContext;
import org.folio.spring.FolioExecutionContextService;
import org.folio.spring.FolioModuleMetadata;
import org.folio.spring.utils.RequestUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.folio.spring.integration.XOkapiHeaders.OKAPI_HEADERS_PREFIX;
import static org.folio.spring.integration.XOkapiHeaders.TENANT;
import static org.folio.spring.integration.XOkapiHeaders.TOKEN;
import static org.folio.spring.integration.XOkapiHeaders.URL;

@Slf4j
public class DefaultFolioExecutionContextService implements FolioExecutionContextService {
  private final ThreadLocal<FolioExecutionContext> folioExecutionContextThreadLocal = new ThreadLocal<>();

  private final FolioModuleMetadata folioModuleMetadata;

  private final FolioExecutionContext emptyFolioExecutionContext;

  public DefaultFolioExecutionContextService(FolioModuleMetadata folioModuleMetadata) {
    this.folioModuleMetadata = folioModuleMetadata;
    emptyFolioExecutionContext = new EmptyFolioExecutionContext(folioModuleMetadata);
  }

  @Override
  public FolioExecutionContext getFolioExecutionContext() {
    return getOrCreateFolioExecutionContext();
  }

  @Override
  public void contextBegin(FolioExecutionContext context) {
    folioExecutionContextThreadLocal.set(context);
  }

  @Override
  public FolioExecutionContext contextEnd() {
    var context = folioExecutionContextThreadLocal.get();
    folioExecutionContextThreadLocal.remove();
    return context;
  }

  private FolioExecutionContext getOrCreateFolioExecutionContext() {
    var context = folioExecutionContextThreadLocal.get();
    return context != null ? context : createFolioExecutionContextFromRequest();
  }

  private FolioExecutionContext createFolioExecutionContextFromRequest() {
    var httpHeadersFromRequest = RequestUtils.getHttpHeadersFromRequest();
    return httpHeadersFromRequest != null ?
      new DefaultFolioExecutionContext(folioModuleMetadata, httpHeadersFromRequest) : emptyFolioExecutionContext;
  }

  private static class DefaultFolioExecutionContext implements FolioExecutionContext {
    private final FolioModuleMetadata folioModuleMetadata;
    private final Map<String, Collection<String>> allHeaders;
    private final Map<String, Collection<String>> okapiHeaders;

    private final String tenantId;
    private final String okapiUrl;
    private final String token;
    private final String userName;

    private DefaultFolioExecutionContext(FolioModuleMetadata folioModuleMetadata, Map<String, Collection<String>> allHeaders) {
      this.folioModuleMetadata = folioModuleMetadata;
      this.allHeaders = allHeaders;
      this.okapiHeaders = new HashMap<>(allHeaders);
      this.okapiHeaders.entrySet().removeIf(e -> !e.getKey().toLowerCase().startsWith(OKAPI_HEADERS_PREFIX));

      this.tenantId = retrieveFirstSafe(okapiHeaders.get(TENANT));
      this.okapiUrl = retrieveFirstSafe(okapiHeaders.get(URL));
      this.token = retrieveFirstSafe(okapiHeaders.get(TOKEN));
      //TODO: retrieve user name correctly
      this.userName = "NO_USER";
    }

    private String retrieveFirstSafe(Collection<String> strings) {
      return strings != null && !strings.isEmpty() ? strings.iterator().next() : "";
    }

    @Override
    public String getTenantId() {
      return tenantId;
    }

    @Override
    public String getOkapiUrl() {
      return okapiUrl;
    }

    @Override
    public String getToken() {
      return token;
    }

    @Override
    public String getUserName() {
      return userName;
    }

    @Override
    public Map<String, Collection<String>> getAllHeaders() {
      return allHeaders;
    }

    @Override
    public Map<String, Collection<String>> getOkapiHeaders() {
      return okapiHeaders;
    }

    @Override
    public FolioModuleMetadata getFolioModuleMetadata() {
      return folioModuleMetadata;
    }
  }

  private static class EmptyFolioExecutionContext implements FolioExecutionContext {
    private final FolioModuleMetadata folioModuleMetadata;

    private EmptyFolioExecutionContext(FolioModuleMetadata folioModuleMetadata) {
      this.folioModuleMetadata = folioModuleMetadata;
    }

    @Override
    public String getTenantId() {
      return null;
    }

    @Override
    public String getOkapiUrl() {
      return null;
    }

    @Override
    public String getToken() {
      return null;
    }

    @Override
    public String getUserName() {
      return null;
    }

    @Override
    public Map<String, Collection<String>> getAllHeaders() {
      return null;
    }

    @Override
    public Map<String, Collection<String>> getOkapiHeaders() {
      return null;
    }

    @Override
    public FolioModuleMetadata getFolioModuleMetadata() {
      return folioModuleMetadata;
    }
  }

}
