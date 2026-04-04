package co.ke.xently.common.headers.validators;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public sealed interface ValidationResult permits ValidationResult.Failure, ValidationResult.Success {
    record Success() implements ValidationResult {
    }

    record Failure(@NonNull String errorMessage, @Nullable Throwable cause) implements ValidationResult {
        public Failure(@NonNull String errorMessage) {
            this(errorMessage, null);
        }
    }
}
