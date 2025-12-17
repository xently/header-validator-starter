package com.kcbgroup.common.utils;

import com.kcbgroup.common.KCBRequestContextHolder;
import com.kcbgroup.common.headers.exceptions.HeadersValidationException;
import com.kcbgroup.common.utils.converter.PayloadConverter;
import com.kcbgroup.common.utils.dto.ResponsePayload;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Slf4j
@AllArgsConstructor
@Component
public class HeaderValidationErrorResponseHandler {
    private final PayloadConverter converter;

    public Object handleHeadersValidationException(HeadersValidationException exception) {
        var requestContext = KCBRequestContextHolder.getContext();

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

        var response = ResponsePayload.builder()
                .statusCode("0")
                .statusDescription("Failed")
                .messageCode("4000453")
                .messageDescription("Invalid or missing request headers")
                .errorInfo(errors)
                .messageID(requestContext.messageID())
                .conversationID(requestContext.conversationID())
                .additionalData(Collections.emptyList())
                .build();
        return converter.convertToHeaderValidationErrorResponse(response, exception);
    }
}
