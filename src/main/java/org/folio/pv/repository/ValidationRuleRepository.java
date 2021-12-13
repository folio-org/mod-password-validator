package org.folio.pv.repository;

import java.util.List;
import java.util.UUID;
import org.folio.pv.domain.entity.PasswordValidationRule;
import org.folio.spring.cql.JpaCqlRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ValidationRuleRepository extends JpaCqlRepository<PasswordValidationRule, UUID> {
  List<PasswordValidationRule> findByRuleState(String ruleState);
}
