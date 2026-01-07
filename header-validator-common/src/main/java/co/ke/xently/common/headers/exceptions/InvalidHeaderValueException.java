package co.ke.xently.common.headers.exceptions;

import co.ke.xently.common.headers.HeaderRule;
import co.ke.xently.common.headers.validators.ValidationResult;
import lombok.Getter;
import org.jspecify.annotations.NonNull;

@Getter
public final class InvalidHeaderValueException extends HeaderException {
    public InvalidHeaderValueException(@NonNull HeaderRule rule, ValidationResult.@NonNull Failure failure) {
        super(rule, failure);
    }
}
