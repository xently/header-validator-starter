package co.ke.xently.common.utils;

import co.ke.xently.common.headers.HeaderRule;
import co.ke.xently.common.headers.exceptions.HeadersValidationException;
import co.ke.xently.common.headers.exceptions.InvalidHeaderValueException;
import co.ke.xently.common.headers.exceptions.MissingHeaderException;
import co.ke.xently.common.headers.validators.ValidationResult;
import co.ke.xently.common.utils.dto.ResponsePayload;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertLinesMatch;

class HeaderValidationErrorResponseHandlerTest {
    @Nested
    class handleHeadersValidationException {
        @Test
        void shouldMapExceptionsToResponsePayload() {
            var missingRule = HeaderRule.builder().headerName("X-FeatureName").required(true).build();
            var invalidRule = HeaderRule.builder().headerName("X-MinorServiceVersion").required(true).build();

            var ex = new HeadersValidationException();
            ex.addHeaderException(new MissingHeaderException(missingRule));
            ex.addHeaderException(new InvalidHeaderValueException(invalidRule, new ValidationResult.Failure("Invalid value")));
            var handler = new HeaderValidationErrorResponseHandler();

            var actual = handler.handleHeadersValidationException(ex);

            assertAll(
                    () -> assertThat(actual.messageCode())
                            .isEqualTo("4000453"),
                    () -> assertThat(actual.errorInfo())
                            .isNotNull(),
                    () -> assertThat(actual.errorInfo().size())
                            .isEqualTo(2),
                    () -> assertThat(actual.errorInfo().stream().anyMatch(e ->
                            e.errorCode().equals("X-FeatureName") &&
                                    e.errorDescription().equals("Missing required header")))
                            .isTrue(),
                    () -> assertLinesMatch(
                            Stream.of("Invalid value"),
                            actual.errorInfo()
                                    .stream()
                                    .filter(e -> e.errorCode().equals("X-MinorServiceVersion"))
                                    .map(ResponsePayload.ErrorInfo::errorDescription)
                    )
            );
        }
    }
}