package com.kcbgroup.common;

import com.kcbgroup.common.headers.exceptions.HeadersValidationException;
import com.kcbgroup.common.utils.HeaderValidationErrorResponseHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@RequiredArgsConstructor
final class HeaderValidatorExceptionHandler {
    private final HeaderValidationErrorResponseHandler errorResponseHandler;

    @ExceptionHandler(HeadersValidationException.class)
    ResponseEntity<?> handleHeadersValidationException(HeadersValidationException exception) {
        var response = errorResponseHandler.handleHeadersValidationException(exception);
        return new ResponseEntity<>(response, exception.getStatusCode());
    }
}
