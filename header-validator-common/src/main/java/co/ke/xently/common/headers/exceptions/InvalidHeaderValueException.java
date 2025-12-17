package co.ke.xently.common.headers.exceptions;

import co.ke.xently.common.headers.HeaderRule;
import co.ke.xently.common.headers.validators.ValidationResult;
import lombok.Getter;
import org.springframework.lang.NonNull;

@Getter
public final class InvalidHeaderValueException extends HeaderException {
    public InvalidHeaderValueException(@NonNull HeaderRule rule, @NonNull ValidationResult.Failure failure) {
        super(rule, failure);
    }
}
