package com.kcbgroup.common;

import com.kcbgroup.common.headers.HeaderRule;
import com.kcbgroup.common.headers.exceptions.HeadersValidationException;
import com.kcbgroup.common.headers.exceptions.MissingHeaderException;
import com.kcbgroup.common.utils.HeaderValidationErrorResponseHandler;
import com.kcbgroup.common.utils.converter.PayloadConverter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class HeaderValidatorExceptionHandlerTest {
    @AfterEach
    void tearDown() {
        try {
            KCBRequestContextHolder.clear();
        } catch (Exception ignored) {
        }
    }

    @Nested
    class handleHeadersValidationException {
        @Test
        void shouldMapToBadRequest() {
            KCBRequestContextHolder.setContext(new KCBRequestContext("conv-001", "msg-001"));

            var missingRule = HeaderRule.builder().headerName("X-FeatureName").required(true).build();

            var ex = new HeadersValidationException();
            ex.addHeaderException(new MissingHeaderException(missingRule));
            var errorResponseHandler = new HeaderValidationErrorResponseHandler(new PayloadConverter() {
            });
            var handler = new HeaderValidatorExceptionHandler(errorResponseHandler);

            var actual = handler.handleHeadersValidationException(ex);

            assertThat(actual.getStatusCode())
                    .isEqualTo(HttpStatus.BAD_REQUEST);
        }
    }
}