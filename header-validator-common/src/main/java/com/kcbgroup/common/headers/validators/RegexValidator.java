package com.kcbgroup.common.headers.validators;

import org.springframework.lang.NonNull;

import java.util.regex.Pattern;

public record RegexValidator(@NonNull Pattern pattern) implements HeaderValidator {
    public RegexValidator(String pattern) {
        this(Pattern.compile(pattern));
    }

    @Override
    @NonNull
    public ValidationResult validate(@NonNull String headerName, @NonNull String headerValue) {
        return pattern.matcher(headerValue).matches()
                ? new ValidationResult.Success()
                : new ValidationResult.Failure("Header value '%s' does not match pattern '%s'".formatted(headerValue, pattern.pattern()));
    }
}