package com.kcbgroup.common;

import com.kcbgroup.common.headers.exceptions.HeadersValidationException;
import com.kcbgroup.common.utils.HeaderValidationErrorResponseHandler;
import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.AbstractErrorWebExceptionHandler;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@Order(-2) // Priority higher than DefaultErrorWebExceptionHandler (-1)
final class HeaderValidatorExceptionHandler extends AbstractErrorWebExceptionHandler {
    private final HeaderValidationErrorResponseHandler errorResponseHandler;

    public HeaderValidatorExceptionHandler(
            ErrorAttributes errorAttributes,
            WebProperties webProperties,
            ApplicationContext applicationContext,
            ServerCodecConfigurer serverCodecConfigurer,
            HeaderValidationErrorResponseHandler errorResponseHandler
    ) {
        super(errorAttributes, webProperties.getResources(), applicationContext);
        this.errorResponseHandler = errorResponseHandler;
        super.setMessageWriters(serverCodecConfigurer.getWriters());
        super.setMessageReaders(serverCodecConfigurer.getReaders());
    }

    @Override
    protected RouterFunction<ServerResponse> getRoutingFunction(ErrorAttributes errorAttributes) {
        return RouterFunctions.route(RequestPredicates.all(), request -> {
            var error = getError(request);

            // If it's not our specific exception, pass it to the next handler (optional, or handle generic)
            if (!(error instanceof HeadersValidationException exception)) {
                return Mono.error(error);
            }

            var response = errorResponseHandler.handleHeadersValidationException(exception);

            return ServerResponse.status(exception.getStatusCode())
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(response);
        });
    }
}
