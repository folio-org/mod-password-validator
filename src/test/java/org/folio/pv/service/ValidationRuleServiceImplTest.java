package org.folio.pv.service;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.when;

import io.github.glytching.junit.extension.random.Random;
import io.github.glytching.junit.extension.random.RandomBeansExtension;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.folio.pv.client.UserClient;
import org.folio.pv.domain.dto.ValidationRule;
import org.folio.pv.domain.dto.ValidationRuleCollection;
import org.folio.pv.domain.entity.PasswordValidationRule;
import org.folio.pv.mapper.ValidationRuleMapper;
import org.folio.pv.repository.ValidationRuleRepository;
import org.folio.pv.service.validator.ValidatorRegistry;
import org.folio.spring.data.OffsetRequest;
import org.folio.spring.test.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@UnitTest
@ExtendWith({
  RandomBeansExtension.class,
  SpringExtension.class
})
public class ValidationRuleServiceImplTest {

  @MockitoBean
  private ValidationRuleMapper mapper;
  @MockitoBean
  private ValidationRuleRepository repository;
  @MockitoBean
  private UserClient userClient;
  @MockitoBean
  private ValidatorRegistry validationRegistry;

  @Autowired
  private ValidationRuleService service;

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
  void shouldReturnValidationRules(@Random Integer offset, @Random Integer limit, @Random String cql,
                                   @Random List<PasswordValidationRule> rules,
                                   @Random ValidationRuleCollection ruleCollection) {

    int o = Math.abs(offset);
    int l = Math.abs(limit);
    OffsetRequest offsetReq = new OffsetRequest(o, l);
    Page<PasswordValidationRule> rulePage = new PageImpl<>(rules);

    when(repository.findByCql(cql, offsetReq)).thenReturn(rulePage);
    when(mapper.mapEntitiesToValidationRuleCollection(rulePage)).thenReturn(ruleCollection);

    ValidationRuleCollection result = service.getValidationRules(o, l, cql);

    assertSame(ruleCollection, result);
  }

  @Test
  void shouldReturnValidationRulesWithoutCql(@Random Integer offset, @Random Integer limit,
                                             @Random List<PasswordValidationRule> rules,
                                             @Random ValidationRuleCollection ruleCollection) {

    var o = Math.abs(offset);
    var l = Math.abs(limit);
    var offsetReq = new OffsetRequest(o, l);
    var rulePage = new PageImpl<>(rules);

    when(repository.findAll(offsetReq)).thenReturn(rulePage);
    when(mapper.mapEntitiesToValidationRuleCollection(rulePage)).thenReturn(ruleCollection);

    var result = service.getValidationRules(o, l, null);

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

  @Test
  void shouldCreateValidationRule(@Random ValidationRule ruleDto, @Random PasswordValidationRule rule) {
    rule.setId(null);
    rule.setCreatedDate(null);

    when(mapper.mapDtoToEntity(ruleDto)).thenReturn(rule);
    when(repository.save(rule)).thenReturn(rule);
    when(mapper.mapEntityToDto(rule)).thenReturn(ruleDto);

    ValidationRule result = service.createOrUpdateValidationRule(ruleDto);

    assertSame(ruleDto, result);
  }

  @Test
  void shouldUpdateValidationRule(@Random ValidationRule ruleDto, @Random PasswordValidationRule rule) {
    when(mapper.mapDtoToEntity(ruleDto)).thenReturn(rule);
    when(repository.getById(rule.getId())).thenReturn(rule);
    when(repository.save(rule)).thenReturn(rule);
    when(mapper.mapEntityToDto(rule)).thenReturn(ruleDto);

    ValidationRule result = service.createOrUpdateValidationRule(ruleDto);

    assertSame(ruleDto, result);
  }

  @TestConfiguration
  public static class Config {

    @Bean
    public ValidationRuleService employeeService(ValidationRuleMapper mapper, ValidationRuleRepository repository) {
      return new ValidationRuleServiceImpl(mapper, repository);
    }
  }
}
