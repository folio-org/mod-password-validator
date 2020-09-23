package org.folio.spring.config;

import org.apache.commons.lang3.StringUtils;
import org.folio.spring.FolioExecutionContextService;
import org.springframework.jdbc.datasource.DelegatingDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class DataSourceFolioWrapper extends DelegatingDataSource {
  private final FolioExecutionContextService folioExecutionContextService;

  public DataSourceFolioWrapper(DataSource targetDataSource, FolioExecutionContextService folioExecutionContextService) {
    super(targetDataSource);
    this.folioExecutionContextService = folioExecutionContextService;
  }

  private Connection prepareConnectionSafe(Connection connection) throws SQLException {
    if (connection != null) {
      var folioExecutionContext = folioExecutionContextService.getFolioExecutionContext();

      var tenantId = folioExecutionContext.getTenantId();
      try (var statement = connection.prepareStatement(
        String.format(
          "SET search_path = %s;",
          StringUtils.isBlank(tenantId) ? "public" : folioExecutionContext.getFolioModuleMetadata().getDBSchemaName(tenantId) + ", public")
      )) {
        statement.execute();
      }

      return connection;
    }
    return null;
  }

  @Override
  public Connection getConnection() throws SQLException {
    return prepareConnectionSafe(obtainTargetDataSource().getConnection());
  }

  @Override
  public Connection getConnection(String username, String password) throws SQLException {
    return prepareConnectionSafe(obtainTargetDataSource().getConnection(username, password));
  }
}
