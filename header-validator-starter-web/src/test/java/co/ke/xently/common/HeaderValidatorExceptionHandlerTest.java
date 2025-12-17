package co.ke.xently.common;

import co.ke.xently.common.headers.HeaderRule;
import co.ke.xently.common.headers.exceptions.HeadersValidationException;
import co.ke.xently.common.headers.exceptions.MissingHeaderException;
import co.ke.xently.common.utils.HeaderValidationErrorResponseHandler;
import co.ke.xently.common.utils.converter.PayloadConverter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class HeaderValidatorExceptionHandlerTest {
    @AfterEach
    void tearDown() {
        try {
            RequestContextHolder.clear();
        } catch (Exception ignored) {
        }
    }

    @Nested
    class handleHeadersValidationException {
        @Test
        void shouldMapToBadRequest() {
            RequestContextHolder.setContext(new RequestContext("conv-001", "msg-001"));

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