package org.folio.pv.service;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import io.github.glytching.junit.extension.random.Random;
import io.github.glytching.junit.extension.random.RandomBeansExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import org.folio.pv.client.UserClient;
import org.folio.pv.domain.dto.ValidationRule;
import org.folio.pv.domain.dto.ValidationRuleCollection;
import org.folio.pv.domain.entity.PasswordValidationRule;
import org.folio.pv.mapper.ValidationRuleMapper;
import org.folio.pv.repository.ValidationRuleRepository;
import org.folio.pv.service.validator.ValidatorRegistry;
import org.folio.spring.data.OffsetRequest;

@ExtendWith({
  /*  MockitoExtension.class,*/
    RandomBeansExtension.class
})
@ExtendWith(SpringExtension.class)
class ValidationRuleServiceImplTest {

  @MockBean
  private ValidationRuleMapper mapper;
  @MockBean
  private ValidationRuleRepository repository;
  @MockBean
  private UserClient userClient;
  @MockBean
  private ValidatorRegistry validationRegistry;

  @Autowired
  private ValidationRuleService service;

  @TestConfiguration
  static class Config {

    @Bean
    public ValidationRuleService employeeService(ValidationRuleMapper mapper, ValidationRuleRepository repository,
        UserClient userClient, ValidatorRegistry validationRegistry) {
      return new ValidationRuleServiceImpl(mapper, repository, userClient, validationRegistry);
    }
  }

  @Test
  void shouldReturnValidationRuleById(@Random UUID ruleId, @Random PasswordValidationRule rule,
      @Random ValidationRule ruleDto) {
    
    when(repository.findById(ruleId)).thenReturn(Optional.of(rule));
    when(mapper.mapEntityToDto(rule)).thenReturn(ruleDto);

    ValidationRule result = service.getValidationRuleById(ruleId.toString());

    assertSame(ruleDto, result);
  }

  @Test
  void shouldReturnNullIfValidationRuleNotFoundById(@Random UUID ruleId) {
    when(repository.findById(ruleId)).thenReturn(Optional.empty());

    ValidationRule result = service.getValidationRuleById(ruleId.toString());

    assertNull(result);
  }

  @Test
  void shouldReturnValidationRules(@Random Integer offset, @Random Integer limit, @Random String orderBy,
      @Random List<PasswordValidationRule> rules, @Random ValidationRuleCollection ruleCollection) {

    OffsetRequest offsetReq = new OffsetRequest(offset, limit);
    Page<PasswordValidationRule> rulePage = new PageImpl<>(rules);

    when(repository.findAll(eq(offsetReq))).thenReturn(rulePage);
    when(mapper.mapEntitiesToValidationRuleCollection(rulePage)).thenReturn(ruleCollection);

    ValidationRuleCollection result = service.getValidationRules(offset, limit, orderBy);

    assertSame(ruleCollection, result);
  }

  @Test
  void shouldStoreValidationRule(@Random ValidationRule ruleDto, @Random PasswordValidationRule rule) {
    when(mapper.mapDtoToEntity(ruleDto)).thenReturn(rule);

    PasswordValidationRule storedRule = new PasswordValidationRule().copyForUpdate(rule);
    when(repository.save(rule)).thenReturn(storedRule);

    ValidationRule expected = new ValidationRule();
    when(mapper.mapEntityToDto(storedRule)).thenReturn(expected);

    ValidationRule result = service.storeValidationRule(ruleDto);

    assertSame(expected, result);
  }
}