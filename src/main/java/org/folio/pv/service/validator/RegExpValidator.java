package org.folio.pv.service.validator;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.util.regex.Pattern;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.pv.domain.dto.UserData;
import org.folio.pv.domain.dto.ValidationErrors;
import org.folio.pv.domain.entity.PasswordValidationRule;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
@Log4j2
public class RegExpValidator implements Validator {

  private static final String REGEXP_USER_NAME_PLACEHOLDER = "<USER_NAME>";

  private final PasswordValidationRule rule;


  @Override
  public ValidationErrors validate(String password, UserData user) {
    var expression = rule.getRuleExpression();

    var failed = false;
    if (isNotBlank(expression)) {
      String passwordWithoutSpaces = password.replaceAll("\\s", "");
      String usernameWithoutSpaces = user.getName().replaceAll("\\s", "");

      var exprWithUser = expression.replace(REGEXP_USER_NAME_PLACEHOLDER, usernameWithoutSpaces);
      log.info("Validating password against regexp: {}", exprWithUser);

      var pattern = Pattern.compile(exprWithUser);

      failed = !pattern.matcher(passwordWithoutSpaces).matches();
    }

    if (failed) {
      log.warn("Password matching failed, errMsgId: {}", rule.getErrMessageId());
      return ValidationErrors.of(rule.getErrMessageId());
    }
    return ValidationErrors.none();
  }

}
