package org.folio.pv.service;

import static org.apache.commons.lang3.StringUtils.isBlank;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.pv.domain.RuleState;
import org.folio.pv.domain.dto.ValidationRule;
import org.folio.pv.domain.dto.ValidationRuleCollection;
import org.folio.pv.domain.entity.PasswordValidationRule;
import org.folio.pv.mapper.ValidationRuleMapper;
import org.folio.pv.repository.ValidationRuleRepository;
import org.folio.spring.data.OffsetRequest;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = ValidationRuleServiceImpl.VALIDATION_RULES_CACHE)
@Log4j2
public class ValidationRuleServiceImpl implements ValidationRuleService {
  public static final String VALIDATION_RULES_CACHE = "validationRulesCache";

  private final ValidationRuleMapper validationRuleMapper;
  private final ValidationRuleRepository validationRuleRepository;

  @Override
  public ValidationRule getValidationRuleById(String ruleId) {
    var id = UUID.fromString(ruleId);

    return validationRuleRepository.findById(id).map(validationRuleMapper::mapEntityToDto).orElse(null);
  }

  @Override
  public ValidationRuleCollection getValidationRules(Integer offset, Integer limit, String cql) {
    log.debug("getValidationRules:: Attempts to find validationRules by [offset: {}, limit: {}, cql: {}]",
      offset, limit, cql);

    boolean isBlank = isBlank(cql);
    log.info("getValidationRules:: isBlank(cql) is {}", isBlank);

    var validationRuleList = isBlank
      ? validationRuleRepository.findAll(new OffsetRequest(offset, limit))
      : validationRuleRepository.findByCql(cql, new OffsetRequest(offset, limit));
    return validationRuleMapper.mapEntitiesToValidationRuleCollection(validationRuleList);
  }

  @Override
  @CacheEvict(allEntries = true)
  public ValidationRule createOrUpdateValidationRule(ValidationRule validationRule) {
    log.debug("createOrUpdateValidationRule:: by [validationRule: {}]", validationRule);

    var rule = validationRuleMapper.mapDtoToEntity(validationRule);
    if (rule.getId() == null) {
      if (rule.getCreatedDate() == null) {
        log.info("createOrUpdateValidationRule:: rule.getId() & rule.getCreatedDate() is null");
        rule.setCreatedDate(Timestamp.valueOf(LocalDateTime.now()));
      }
    } else {
      log.info("createOrUpdateValidationRule:: rule.getId() is not null");
      rule = validationRuleRepository.getById(rule.getId()).copyForUpdate(rule);
    }
    return validationRuleMapper.mapEntityToDto(validationRuleRepository.save(rule));
  }

  @Override
  @CacheEvict(allEntries = true)
  public ValidationRule storeValidationRule(ValidationRule validationRule) {
    var rule = validationRuleMapper.mapDtoToEntity(validationRule);
    return validationRuleMapper.mapEntityToDto(validationRuleRepository.save(rule));
  }

  @Override
  @Cacheable(key = "#tenant", unless = "#result == null")
  public List<PasswordValidationRule> getEnabledRules(String tenant) {
    return validationRuleRepository.findByRuleState(RuleState.ENABLED);
  }

}
