package co.ke.xently.common.headers.validators;

import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public record DefaultHeaderValidator() implements HeaderValidator {
    @Override
    @NonNull
    public ValidationResult validate(@NonNull String headerName, @NonNull String headerValue) {
        return StringUtils.hasText(headerValue)
                ? new ValidationResult.Success()
                : new ValidationResult.Failure("Header value is required");
    }
}
