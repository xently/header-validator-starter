package com.kcbgroup.common.headers;

import com.kcbgroup.common.headers.exceptions.InvalidHeaderValueException;
import com.kcbgroup.common.headers.exceptions.MissingHeaderException;
import com.kcbgroup.common.headers.validators.RegexValidator;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class HeaderValidationConfigurationTest {
    @Nested
    class validate {
        private final HeaderValidationWebFilter filter = new HeaderValidationWebFilter(new HeaderValidationProperties(Set.of()));

        static Stream<String> shouldThrowInvalidHeaderValueException() {
            return Stream.of("", "  ");
        }

        @Test
        void shouldNotThrowExceptionForRequiredHeaderWithNullValue() {
            var rule = HeaderRule.builder().headerName("X-Req").required(true).build();

            assertThrows(MissingHeaderException.class, () -> filter.validate(rule, null));
        }

        @ParameterizedTest
        @MethodSource("shouldThrowInvalidHeaderValueException")
        void shouldThrowExceptionForRequiredHeaderWithDefaultValidatorWithBlankValue(final String headerValue) {
            var rule = HeaderRule.builder().headerName("X-Req").required(true).build();

            assertThrows(InvalidHeaderValueException.class, () -> filter.validate(rule, headerValue));
        }

        @Test
        void shouldNotThrowExceptionForOptionalHeaderWithNullValue() {
            var rule = HeaderRule.builder().headerName("X-Opt").required(false).build();

            assertDoesNotThrow(() -> filter.validate(rule, null));
        }

        @ParameterizedTest
        @MethodSource("shouldThrowInvalidHeaderValueException")
        void shouldThrowExceptionForOptionalHeaderWithDefaultValidatorWithBlankValue(final String headerValue) {
            var rule = HeaderRule.builder().headerName("X-Opt").required(false).build();

            assertThrows(InvalidHeaderValueException.class, () -> filter.validate(rule, headerValue));
        }

        @Test
        void whenPresentButInvalid_thenThrowsInvalidHeaderValueException() {
            var rule = HeaderRule.builder().headerName("X-Regex").required(true)
                    .validator(new RegexValidator("^v\\d+$")).build();

            assertThrows(InvalidHeaderValueException.class, () -> filter.validate(rule, "bad"));
        }

        @Test
        void whenPresentAndValid_thenPasses() {
            var rule = HeaderRule.builder().headerName("X-Regex").required(true)
                    .validator(new RegexValidator("^v\\d+$")).build();

            assertDoesNotThrow(() -> filter.validate(rule, "v1"));
        }
    }
}
