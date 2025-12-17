package co.ke.xently.common.headers.validators;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public record EpochTimestampValidator() implements HeaderValidator {
    @Override
    @NonNull
    public ValidationResult validate(@NonNull String headerName, @NonNull String headerValue) {
        try {
            new Date(Long.parseLong(headerValue));
            return new ValidationResult.Success();
        } catch (NumberFormatException e) {
            return new ValidationResult.Failure("Header value is not a valid epoch timestamp", e);
        }
    }
}