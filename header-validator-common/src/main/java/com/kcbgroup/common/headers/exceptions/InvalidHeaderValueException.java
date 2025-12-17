package com.kcbgroup.common.headers.exceptions;

import com.kcbgroup.common.headers.HeaderRule;
import com.kcbgroup.common.headers.validators.ValidationResult;
import lombok.Getter;
import org.springframework.lang.NonNull;

@Getter
public final class InvalidHeaderValueException extends HeaderException {
    public InvalidHeaderValueException(@NonNull HeaderRule rule, @NonNull ValidationResult.Failure failure) {
        super(rule, failure);
    }
}
