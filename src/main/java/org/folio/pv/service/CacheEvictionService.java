package org.folio.pv.service;

import static org.folio.pv.service.ValidationRuleServiceImpl.VALIDATION_RULES_CACHE;

import lombok.extern.log4j.Log4j2;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class CacheEvictionService {

  @CacheEvict(value = {VALIDATION_RULES_CACHE}, allEntries = true)
  @Scheduled(
      fixedRateString = "${cache.eviction.time.validation-rule}",
      initialDelayString = "${cache.eviction.time.validation-rule}"
  )
  public void clearInstanceDataCache() {
    log.info("Cleared cache entries for {}", VALIDATION_RULES_CACHE);
  }

}
