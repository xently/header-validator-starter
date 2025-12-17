package com.kcbgroup.common.headers.validators;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

public sealed interface ValidationResult permits ValidationResult.Failure, ValidationResult.Success {
    record Success() implements ValidationResult {
    }

    record Failure(@NonNull String errorMessage, @Nullable Throwable cause) implements ValidationResult {
        public Failure(@NonNull String errorMessage) {
            this(errorMessage, null);
        }
    }
}
