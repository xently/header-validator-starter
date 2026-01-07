package co.ke.xently.common.headers.exceptions;

import co.ke.xently.common.headers.HeaderRule;
import co.ke.xently.common.headers.validators.ValidationResult;
import org.jspecify.annotations.NonNull;

public final class MissingHeaderException extends HeaderException {
    public MissingHeaderException(@NonNull HeaderRule rule) {
        super(rule, new ValidationResult.Failure("Missing required header"));
    }
}
