package org.folio.pv.testutils;


import static org.folio.pv.testutils.ApiTestUtils.TENANT_ID;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import org.folio.pv.domain.RuleState;
import org.folio.pv.domain.RuleType;
import org.folio.pv.domain.ValidationType;
import org.folio.pv.domain.entity.PasswordValidationRule;
import org.folio.spring.FolioModuleMetadata;
import org.springframework.jdbc.core.JdbcTemplate;

public class DbTestUtils {

  public static final String VALIDATION_RULES_TABLE_NAME = "validationrules";

  private final FolioModuleMetadata metadata;

  private final JdbcTemplate jdbcTemplate;

  public DbTestUtils(FolioModuleMetadata metadata, JdbcTemplate jdbcTemplate) {
    this.metadata = metadata;
    this.jdbcTemplate = jdbcTemplate;
  }

  public PasswordValidationRule getValidationRuleById(UUID id) {
    var sql = "SELECT * FROM " + validationRulesTable(TENANT_ID, metadata) + " WHERE id = ?";
    return jdbcTemplate.query(sql, rs -> {
      rs.next();
      var rule = new PasswordValidationRule();
      rule.setId(getUuid(rs));
      rule.setRuleState(RuleState.fromValue(rs.getString("rule_state")));
      rule.setRuleType(RuleType.fromValue(rs.getString("rule_type")));
      rule.setValidationType(ValidationType.fromValue(rs.getString("validation_type")));
      rule.setName(rs.getString("name"));
      rule.setErrMessageId(rs.getString("err_message_id"));
      rule.setRuleExpression(rs.getString("rule_expression"));
      rule.setCreatedDate(rs.getTimestamp("created_date"));
      rule.setUpdatedDate(rs.getTimestamp("updated_date"));
      rule.setOrderNo(rs.getInt("order_no"));
      return rule;
    }, id);
  }

  public static String validationRulesTable(String tenantId, FolioModuleMetadata metadata) {
    return getTableName(VALIDATION_RULES_TABLE_NAME, tenantId, metadata);
  }

  public static String getTableName(String tableName, String tenantId, FolioModuleMetadata metadata) {
    return metadata.getDBSchemaName(tenantId) + "." + tableName;
  }

  private static UUID getUuid(ResultSet rs) throws SQLException {
    var string = rs.getString("id");
    return string == null ? null : UUID.fromString(string);
  }
}
