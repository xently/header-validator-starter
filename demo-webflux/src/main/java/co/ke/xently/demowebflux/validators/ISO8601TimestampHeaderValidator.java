package co.ke.xently.demowebflux.validators;


import co.ke.xently.common.headers.validators.HeaderValidator;
import co.ke.xently.common.headers.validators.ValidationResult;
import org.jspecify.annotations.NonNull;

import java.time.Instant;
import java.time.format.DateTimeParseException;

@SuppressWarnings("unused")
public class ISO8601TimestampHeaderValidator implements HeaderValidator {
    @Override
    @NonNull
    public ValidationResult validate(@NonNull String headerName, @NonNull String headerValue) {
        try {
            Instant.parse(headerValue);
            return new ValidationResult.Success();
        } catch (DateTimeParseException e) {
            return new ValidationResult.Failure("Invalid timestamp format. Expected ISO-8601 format: yyyy-MM-dd'T'HH:mm:ss.SSSZ", e);
        }
    }
}