package com.kcbgroup.demowebflux.validators;

import com.kcbgroup.common.headers.validators.HeaderValidator;
import com.kcbgroup.common.headers.validators.ValidationResult;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;


@Repository
class ExternalService {
    boolean hasAtLeastXUpperCaseLetters(String value, int x) {
        return value.chars()
                .filter(Character::isUpperCase)
                .count() >= x;
    }
}

@Component(value = "CustomValidator")
@RequiredArgsConstructor
final class CustomValidator implements HeaderValidator {
    @Value("${demo.permissible.uppercase.letters.count:1}")
    private int permissibleUppercaseLetters;
    private final ExternalService service;

    @Override
    @NonNull
    public ValidationResult validate(@NonNull String headerName, @NonNull String headerValue) {
        if (headerValue.equalsIgnoreCase(headerName) && service.hasAtLeastXUpperCaseLetters(headerValue, permissibleUppercaseLetters)) {
            return new ValidationResult.Success();
        }
        return new ValidationResult.Failure("Header value '%s' should be '%s' with at least %d uppercase letters".formatted(headerValue, headerName, permissibleUppercaseLetters));
    }
}
