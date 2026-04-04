package co.ke.xently.common.headers.validators;

import org.jspecify.annotations.NonNull;

@FunctionalInterface
public interface HeaderValidator {
    @NonNull
    ValidationResult validate(@NonNull String headerName, @NonNull String headerValue);
}
