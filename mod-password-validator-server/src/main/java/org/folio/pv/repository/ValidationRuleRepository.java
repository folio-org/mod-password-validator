package org.folio.pv.repository;

import org.folio.pv.domain.entity.PasswordValidationRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ValidationRuleRepository extends JpaRepository<PasswordValidationRule, UUID> {
  Optional<PasswordValidationRule> findById(UUID id);
}
