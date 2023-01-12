package org.folio.pv.mapper;

import java.util.List;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.folio.pv.domain.RuleState;
import org.folio.pv.domain.RuleType;
import org.folio.pv.domain.ValidationType;
import org.folio.pv.domain.dto.ValidationRule;
import org.folio.pv.domain.dto.ValidationRuleCollection;
import org.folio.pv.domain.entity.PasswordValidationRule;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.NullValueCheckStrategy;

@Mapper(componentModel = "spring",
  nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
  imports = {RuleType.class, RuleState.class, ValidationType.class})
public interface ValidationRuleMapper {

  @Mapping(target = "id", expression = "java(uuidToStringSafe(passwordValidationRule.getId()))")
  @Mapping(target = "name", source = "name")
  @Mapping(target = "type", expression = "java(passwordValidationRule.getRuleType() != null ? "
    + "ValidationRule.TypeEnum.fromValue(passwordValidationRule.getRuleType().getValue()) : null)")
  @Mapping(target = "state", expression = "java(passwordValidationRule.getRuleState() != null ? "
    + "ValidationRule.StateEnum.fromValue(passwordValidationRule.getRuleState().getValue()) : null)")
  @Mapping(target = "validationType", expression = "java(passwordValidationRule.getValidationType() != null ? "
    + "ValidationRule.ValidationTypeEnum.fromValue(passwordValidationRule.getValidationType().getValue()) : null)")
  @Mapping(target = "moduleName", source = "moduleName")
  @Mapping(target = "implementationReference", source = "implementationReference")
  @Mapping(target = "expression", source = "ruleExpression")
  @Mapping(target = "description", source = "description")
  @Mapping(target = "orderNo", source = "orderNo")
  @Mapping(target = "errMessageId", source = "errMessageId")
  @Mapping(target = "metadata.createdDate", source = "createdDate")
  @Mapping(target = "metadata.updatedDate", source = "updatedDate")
  @Mapping(target = "metadata.createdByUserId",
    expression = "java(passwordValidationRule.getCreatedByUserId() == null ? "
      + "null : String.valueOf(passwordValidationRule.getCreatedByUserId()))")
  @Mapping(target = "metadata.updatedByUserId",
    expression = "java(passwordValidationRule.getUpdatedByUserId() == null ? "
      + "null : String.valueOf(passwordValidationRule.getUpdatedByUserId()))")
  @Mapping(target = "metadata.createdByUsername", source = "createdByUsername")
  @Mapping(target = "metadata.updatedByUsername", source = "updatedByUsername")
  ValidationRule mapEntityToDto(PasswordValidationRule passwordValidationRule);

  @Mapping(target = "id", expression = "java(stringToUuidSafe(validationRule.getId()))")
  @Mapping(target = "ruleType", expression = "java(validationRule.getType() != null ? "
    + "RuleType.fromValue(validationRule.getType().getValue()) : null)")
  @Mapping(target = "ruleState", expression = "java(validationRule.getState() != null ? "
    + "RuleState.fromValue(validationRule.getState().getValue()) : null)")
  @Mapping(target = "validationType", expression = "java(validationRule.getState() != null ? "
    + "ValidationType.fromValue(validationRule.getValidationType().getValue()) : null)")
  @Mapping(target = "createdByUserId", expression = "java(validationRule.getMetadata() == null ? "
    + "null : stringToUuidSafe(validationRule.getMetadata().getCreatedByUserId()))")
  @Mapping(target = "updatedByUserId", expression = "java(validationRule.getMetadata() == null ? "
    + "null : stringToUuidSafe(validationRule.getMetadata().getUpdatedByUserId()))")
  @InheritInverseConfiguration
  PasswordValidationRule mapDtoToEntity(ValidationRule validationRule);

  @Mappings({})
  List<ValidationRule> mapEntitiesToDtos(Iterable<PasswordValidationRule> passwordValidationRuleList);

  @InheritInverseConfiguration
  List<PasswordValidationRule> mapDtosToEntities(List<ValidationRule> validationRuleList);

  default ValidationRuleCollection mapEntitiesToValidationRuleCollection(
    Iterable<PasswordValidationRule> passwordValidationRuleList) {
    var rules = mapEntitiesToDtos(passwordValidationRuleList);
    return new ValidationRuleCollection().rules(rules).totalRecords(rules.size());
  }

  default UUID stringToUuidSafe(String uuid) {
    return (StringUtils.isBlank(uuid)) ? null : java.util.UUID.fromString(uuid);
  }

  default String uuidToStringSafe(UUID uuid) {
    return uuid != null ? uuid.toString() : null;
  }
}
