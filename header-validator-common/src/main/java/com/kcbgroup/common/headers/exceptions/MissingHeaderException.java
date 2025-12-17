package com.kcbgroup.common.headers.exceptions;

import com.kcbgroup.common.headers.HeaderRule;
import com.kcbgroup.common.headers.validators.ValidationResult;
import org.springframework.lang.NonNull;

public final class MissingHeaderException extends HeaderException {
    public MissingHeaderException(@NonNull HeaderRule rule) {
        super(rule, new ValidationResult.Failure("Missing required header"));
    }
}
