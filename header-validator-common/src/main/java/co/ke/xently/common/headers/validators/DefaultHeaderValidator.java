package co.ke.xently.common.headers.validators;

import org.jspecify.annotations.NonNull;
import org.springframework.util.StringUtils;

import java.util.Objects;
import java.util.Set;

public record DefaultHeaderValidator(Set<String> allowedValues) implements HeaderValidator {
    public DefaultHeaderValidator() {
        this(Set.of());
    }

    @Override
    public @NonNull Set<String> allowedValues() {
        return Objects.requireNonNullElse(allowedValues, Set.of());
    }

    @Override
    @NonNull
    public ValidationResult validate(@NonNull String headerName, @NonNull String headerValue) {
        if (!StringUtils.hasText(headerValue)) return new ValidationResult.Failure("Header value is required");

        var isValid = allowedValues().isEmpty() || allowedValues().stream()
                .anyMatch(headerValue.trim()::equalsIgnoreCase);

        if (isValid) return new ValidationResult.Success();

        return new ValidationResult.Failure("Header value '%s' is not among the expected options: %s".formatted(headerValue, allowedValues().stream().sorted().toList()));
    }
}
