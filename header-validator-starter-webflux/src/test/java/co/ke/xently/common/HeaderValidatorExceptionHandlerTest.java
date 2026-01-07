package co.ke.xently.common;

import co.ke.xently.common.headers.HeaderRule;
import co.ke.xently.common.headers.exceptions.HeadersValidationException;
import co.ke.xently.common.headers.exceptions.InvalidHeaderValueException;
import co.ke.xently.common.headers.exceptions.MissingHeaderException;
import co.ke.xently.common.headers.validators.ValidationResult;
import co.ke.xently.common.utils.HeaderValidationErrorResponseHandler;
import co.ke.xently.common.utils.converter.PayloadConverter;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.webflux.error.ErrorAttributes;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ServerWebExchange;

import java.util.Map;
import java.util.Objects;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class HeaderValidatorExceptionHandlerTest {
    private final PayloadConverter payloadConverter = new PayloadConverter() {
    };
    @AfterEach
    void tearDown() {
        try {
            RequestContextHolder.clear();
        } catch (Exception ignored) {
        }
    }

    @Nested
    class getRoutingFunction {
        @Test
        void shouldRenderHeadersValidationExceptionViaRoutingFunction() {
            RequestContextHolder.setContext(new RequestContext("conv-777", "msg-777"));

            var missingRule = HeaderRule.builder().headerName("X-Alpha").required(true).build();
            var invalidRule = HeaderRule.builder().headerName("X-Beta").required(true).build();

            var ex = new HeadersValidationException();
            ex.addHeaderException(new MissingHeaderException(missingRule));
            ex.addHeaderException(new InvalidHeaderValueException(invalidRule, new ValidationResult.Failure("wrong")));

            class StubErrorAttributes implements ErrorAttributes {
                @Override
                public Throwable getError(@NonNull ServerRequest request) {
                    return ex;
                }

                @NonNull
                @Override
                public Map<String, Object> getErrorAttributes(@NonNull ServerRequest request, @NonNull ErrorAttributeOptions options) {
                    return Map.of();
                }

                @Override
                public void storeErrorInformation(@NonNull Throwable error, @NonNull ServerWebExchange exchange) {
                }
            }

            var errorAttributes = new StubErrorAttributes();
            var properties = new WebProperties();
            var context = new StaticApplicationContext();
            var codecs = ServerCodecConfigurer.create();
            var errorResponseHandler = new HeaderValidationErrorResponseHandler(payloadConverter);

            var handler = new HeaderValidatorExceptionHandler(errorAttributes, properties, context, codecs, errorResponseHandler);
            var routes = handler.getRoutingFunction(errorAttributes);

            var exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/test").build());
            var request = ServerRequest.create(exchange, codecs.getReaders());

            var handlerFunctionMono = routes.route(request);
            var handlerFunction = Objects.requireNonNull(handlerFunctionMono.block());

            var response = Objects.requireNonNull(handlerFunction.handle(request).block());

            assertThat(response.statusCode())
                    .isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        void shouldPropagateNonHeadersExceptionsViaRoutingFunction() {
            class StubErrorAttributes implements ErrorAttributes {
                @Override
                public Throwable getError(@NonNull ServerRequest request) {
                    return new RuntimeException("boom");
                }

                @NonNull
                @Override
                public Map<String, Object> getErrorAttributes(@NonNull ServerRequest request, @NonNull ErrorAttributeOptions options) {
                    return Map.of();
                }

                @Override
                public void storeErrorInformation(@NonNull Throwable error, @NonNull ServerWebExchange exchange) {
                }
            }

            var errorAttributes = new StubErrorAttributes();
            var properties = new WebProperties();
            var context = new StaticApplicationContext();
            var codecs = ServerCodecConfigurer.create();
            var errorResponseHandler = new HeaderValidationErrorResponseHandler(payloadConverter);

            var handler = new HeaderValidatorExceptionHandler(errorAttributes, properties, context, codecs, errorResponseHandler);
            var routes = handler.getRoutingFunction(errorAttributes);

            var exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/non-headers").build());
            var request = ServerRequest.create(exchange, codecs.getReaders());

            var handlerFunction = Objects.requireNonNull(routes.route(request).block());

            assertThrows(RuntimeException.class, () -> handlerFunction.handle(request).block());
        }
    }
}