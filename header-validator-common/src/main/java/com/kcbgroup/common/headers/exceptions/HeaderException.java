package com.kcbgroup.common.headers.exceptions;

import com.kcbgroup.common.headers.HeaderRule;
import com.kcbgroup.common.headers.validators.ValidationResult;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;


@Getter
@RequiredArgsConstructor
public abstract sealed class HeaderException extends Exception permits MissingHeaderException, InvalidHeaderValueException {
    @NonNull
    private final HeaderRule rule;
    @NonNull
    private final ValidationResult.Failure failure;
}
