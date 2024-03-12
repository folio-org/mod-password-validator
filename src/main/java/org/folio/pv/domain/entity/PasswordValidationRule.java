package org.folio.pv.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Data;
import org.folio.pv.domain.RuleState;
import org.folio.pv.domain.RuleType;
import org.folio.pv.domain.ValidationType;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

@Data
@Table(name = "VALIDATIONRULES")
@Entity
public class PasswordValidationRule {

  @Id
  @GeneratedValue
  private UUID id;

  @Column(name = "name")
  private String name;

  @Column(name = "rule_type", columnDefinition = "RuleType")
  @Enumerated(EnumType.STRING)
  @JdbcType(PostgreSQLEnumJdbcType.class)
  private RuleType ruleType;

  @Column(name = "rule_state", columnDefinition = "RuleState")
  @Enumerated(EnumType.STRING)
  @JdbcType(PostgreSQLEnumJdbcType.class)
  private RuleState ruleState;

  @Column(name = "validation_type", columnDefinition = "RuleValidationType")
  @Enumerated(EnumType.STRING)
  @JdbcType(PostgreSQLEnumJdbcType.class)
  private ValidationType validationType;

  @Column(name = "order_no")
  private Integer orderNo;

  @Column(name = "rule_expression")
  private String ruleExpression;

  @Column(name = "implementation_reference")
  private String implementationReference;

  @Column(name = "module_name")
  private String moduleName;

  @Column(name = "description")
  private String description;

  @Column(name = "err_message_id")
  private String errMessageId;

  @Column(name = "created_date")
  private Timestamp createdDate;

  @Column(name = "updated_date")
  private Timestamp updatedDate;

  @Column(name = "created_by_user_id")
  private UUID createdByUserId;

  @Column(name = "updated_by_user_id")
  private UUID updatedByUserId;

  @Column(name = "created_by_username")
  private String createdByUsername;

  @Column(name = "updated_by_username")
  private String updatedByUsername;


  public PasswordValidationRule copyForUpdate(PasswordValidationRule another) {
    this.setRuleType(another.getRuleType());
    this.setRuleState(another.getRuleState());
    this.setValidationType(another.getValidationType());
    this.setOrderNo(another.getOrderNo());
    this.setRuleExpression(another.getRuleExpression());
    this.setImplementationReference(another.getImplementationReference());
    this.setModuleName(another.getModuleName());
    this.setDescription(another.getDescription());
    this.setErrMessageId(another.getErrMessageId());
    this.setUpdatedByUserId(another.getUpdatedByUserId());
    this.setUpdatedByUsername(another.getUpdatedByUsername());
    var ud = another.getUpdatedDate();
    this.setUpdatedDate(ud != null ? ud : Timestamp.valueOf(LocalDateTime.now()));

    return this;
  }
}
