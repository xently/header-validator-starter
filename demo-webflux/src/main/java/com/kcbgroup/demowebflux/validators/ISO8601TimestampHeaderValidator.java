package com.kcbgroup.demowebflux.validators;


import com.kcbgroup.common.headers.validators.HeaderValidator;
import com.kcbgroup.common.headers.validators.ValidationResult;
import org.springframework.lang.NonNull;

import java.time.Instant;
import java.time.format.DateTimeParseException;

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