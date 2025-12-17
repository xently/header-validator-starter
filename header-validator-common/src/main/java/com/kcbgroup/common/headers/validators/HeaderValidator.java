package com.kcbgroup.common.headers.validators;

import org.springframework.lang.NonNull;

@FunctionalInterface
public interface HeaderValidator {
    @NonNull
    ValidationResult validate(@NonNull String headerName, @NonNull String headerValue);
}
