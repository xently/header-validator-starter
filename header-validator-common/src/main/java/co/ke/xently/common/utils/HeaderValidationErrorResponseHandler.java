package co.ke.xently.common.utils;

import co.ke.xently.common.headers.exceptions.HeadersValidationException;
import co.ke.xently.common.utils.dto.ResponsePayload;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@AllArgsConstructor
@Component
public class HeaderValidationErrorResponseHandler {

    public ResponsePayload handleHeadersValidationException(HeadersValidationException exception) {
        var errors = exception.getHeaderExceptions()
                .stream()
                .map(e -> {
                    var failure = e.getFailure();
                    var errorInfo = ResponsePayload.ErrorInfo.builder()
                            .errorCode(e.getRule().getHeaderName())
                            .errorDescription(failure.errorMessage())
                            .build();
                    log.debug("Validation failed for header '{}': {}", errorInfo.errorCode(), errorInfo.errorDescription(), failure.cause());
                    return errorInfo;
                })
                .toList();

        return ResponsePayload.builder()
                .messageCode("4000453")
                .messageDescription("Invalid or missing request headers")
                .errorInfo(errors)
                .build();
    }
}
