package com.kcbgroup.common.headers;

import com.kcbgroup.common.headers.exceptions.HeadersValidationException;
import com.kcbgroup.common.headers.exceptions.InvalidHeaderValueException;
import com.kcbgroup.common.headers.exceptions.MissingHeaderException;
import com.kcbgroup.common.headers.validators.DefaultHeaderValidator;
import com.kcbgroup.common.headers.validators.RegexValidator;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AbstractHeaderValidatorTest {

    private static class TestHeaderValidator extends AbstractHeaderValidator {
        TestHeaderValidator(Set<HeaderRule> rules) {
            super(mockProps(rules));
        }

        private static HeaderValidationProperties mockProps(Set<HeaderRule> rules) {
            var props = mock(HeaderValidationProperties.class);
            when(props.headers())
                    .thenReturn(rules);
            return props;
        }

        HeadersValidationException validateAll(Map<String, String> headers) {
            return createHeadersValidationException(headers::get);
        }
    }

    @Nested
    class createHeadersValidationException {
        @Test
        void shouldReturnEmptyExceptionWhenAllHeadersValid() {
            var rules = Set.of(
                    HeaderRule.builder()
                            .headerName("X-Feature")
                            .required(true)
                            .validator(new DefaultHeaderValidator())
                            .build(),
                    HeaderRule.builder()
                            .headerName("X-Version")
                            .required(false)
                            .validator(new DefaultHeaderValidator())
                            .build()
            );
            var validator = new TestHeaderValidator(rules);

            var ex = validator.validateAll(Map.of(
                    "X-Feature", "feature-name",
                    "X-Version", "1"
            ));

            assertThat(ex.getHeaderExceptions())
                    .isEmpty();
        }

        @Test
        void shouldCollectMissingHeaderExceptionsForRequiredMissing() {
            var rules = Set.of(
                    HeaderRule.builder()
                            .headerName("X-Required")
                            .required(true)
                            .validator(new DefaultHeaderValidator())
                            .build()
            );
            var validator = new TestHeaderValidator(rules);

            var ex = validator.validateAll(Map.of());

            assertAll(
                    () -> assertThat(ex.getHeaderExceptions())
                            .hasSize(1),
                    () -> assertThat(ex.getHeaderExceptions().getFirst())
                            .isInstanceOf(MissingHeaderException.class)
            );
        }

        @Test
        void shouldCollectInvalidHeaderValueException() {
            var rules = Set.of(
                    HeaderRule.builder()
                            .headerName("X-Regex")
                            .required(true)
                            .validator(new RegexValidator("^v\\d+$"))
                            .build()
            );
            var validator = new TestHeaderValidator(rules);

            var ex = validator.validateAll(Map.of("X-Regex", "bad"));

            assertAll(
                    () -> assertThat(ex.getHeaderExceptions())
                            .hasSize(1),
                    () -> assertThat(ex.getHeaderExceptions().getFirst())
                            .isInstanceOf(InvalidHeaderValueException.class)
            );
        }

        @Test
        void shouldIgnoreOptionalMissingHeader() {
            var rules = Set.of(
                    HeaderRule.builder()
                            .headerName("X-Optional")
                            .required(false)
                            .validator(new DefaultHeaderValidator())
                            .build()
            );
            var validator = new TestHeaderValidator(rules);

            var ex = validator.validateAll(Map.of());

            assertThat(ex.getHeaderExceptions())
                    .isEmpty();
        }
    }

    @Nested
    class validate {
        @Test
        void shouldThrowMissingWhenRequiredAndNull() {
            var rule = HeaderRule.builder()
                    .headerName("X-Req")
                    .required(true)
                    .validator(new DefaultHeaderValidator())
                    .build();
            var validator = new TestHeaderValidator(Set.of(rule));

            assertThrows(MissingHeaderException.class, () -> validator.validate(rule, null));
        }

        @Test
        void shouldNotThrowWhenOptionalAndNull() {
            var rule = HeaderRule.builder()
                    .headerName("X-Opt")
                    .required(false)
                    .validator(new DefaultHeaderValidator())
                    .build();
            var validator = new TestHeaderValidator(Set.of(rule));

            assertDoesNotThrow(() -> validator.validate(rule, null));
        }

        @Test
        void shouldThrowInvalidOnFailure() {
            var rule = HeaderRule.builder()
                    .headerName("X-Regex")
                    .required(true)
                    .validator(new RegexValidator("^ok$"))
                    .build();
            var validator = new TestHeaderValidator(Set.of(rule));

            assertThrows(InvalidHeaderValueException.class, () -> validator.validate(rule, "NOPE"));
        }

        @Test
        void shouldPassOnSuccess() {
            var rule = HeaderRule.builder()
                    .headerName("X-Regex")
                    .required(true)
                    .validator(new RegexValidator("^ok$"))
                    .build();
            var validator = new TestHeaderValidator(Set.of(rule));

            assertDoesNotThrow(() -> validator.validate(rule, "ok"));
        }
    }
}
