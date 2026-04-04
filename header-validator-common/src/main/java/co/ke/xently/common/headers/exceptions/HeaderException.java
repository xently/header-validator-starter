package co.ke.xently.common.headers.exceptions;

import co.ke.xently.common.headers.HeaderRule;
import co.ke.xently.common.headers.validators.ValidationResult;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;


@Getter
@RequiredArgsConstructor
public abstract sealed class HeaderException extends Exception permits MissingHeaderException, InvalidHeaderValueException {
    @NonNull
    private final HeaderRule rule;
    private final ValidationResult.@NonNull Failure failure;
}
