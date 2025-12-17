package co.ke.xently.common;

import co.ke.xently.common.headers.exceptions.HeadersValidationException;
import co.ke.xently.common.utils.HeaderValidationErrorResponseHandler;
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
