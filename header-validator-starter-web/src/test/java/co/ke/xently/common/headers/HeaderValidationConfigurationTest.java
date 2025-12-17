package co.ke.xently.common.headers;

import co.ke.xently.common.headers.exceptions.InvalidHeaderValueException;
import co.ke.xently.common.headers.exceptions.MissingHeaderException;
import co.ke.xently.common.headers.validators.RegexValidator;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;

import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HeaderValidationConfigurationTest {

    @Test
    void addInterceptors_registersHeaderValidationInterceptor() {
        var properties = new HeaderValidationProperties(Set.of());
        var config = new HeaderValidationConfiguration(properties);
        var registry = mock(InterceptorRegistry.class);
        var captor = ArgumentCaptor.forClass(HandlerInterceptor.class);
        when(registry.addInterceptor(captor.capture()))
                .thenReturn(null);

        config.addInterceptors(registry);

        var added = captor.getValue();
        assertAll(
                () -> assertNotNull(added),
                () -> assertInstanceOf(HeaderValidationInterceptor.class, added)
        );
    }

    @Nested
    class validate {
        private final HeaderValidationInterceptor interceptor = new HeaderValidationInterceptor(new HeaderValidationProperties(Set.of()));

        static Stream<String> shouldThrowInvalidHeaderValueException() {
            return Stream.of("", "  ");
        }

        @Test
        void shouldNotThrowExceptionForRequiredHeaderWithNullValue() {
            var rule = HeaderRule.builder().headerName("X-Req").required(true).build();

            assertThrows(MissingHeaderException.class, () -> interceptor.validate(rule, null));
        }

        @ParameterizedTest
        @MethodSource("shouldThrowInvalidHeaderValueException")
        void shouldThrowExceptionForRequiredHeaderWithDefaultValidatorWithBlankValue(final String headerValue) {
            var rule = HeaderRule.builder().headerName("X-Req").required(true).build();

            assertThrows(InvalidHeaderValueException.class, () -> interceptor.validate(rule, headerValue));
        }

        @Test
        void shouldNotThrowExceptionForOptionalHeaderWithNullValue() {
            var rule = HeaderRule.builder().headerName("X-Opt").required(false).build();

            assertDoesNotThrow(() -> interceptor.validate(rule, null));
        }

        @ParameterizedTest
        @MethodSource("shouldThrowInvalidHeaderValueException")
        void shouldThrowExceptionForOptionalHeaderWithDefaultValidatorWithBlankValue(final String headerValue) {
            var rule = HeaderRule.builder().headerName("X-Opt").required(false).build();

            assertThrows(InvalidHeaderValueException.class, () -> interceptor.validate(rule, headerValue));
        }

        @Test
        void whenPresentButInvalid_thenThrowsInvalidHeaderValueException() {
            var rule = HeaderRule.builder().headerName("X-Regex").required(true)
                    .validator(new RegexValidator("^v\\d+$")).build();

            assertThrows(InvalidHeaderValueException.class, () -> interceptor.validate(rule, "bad"));
        }

        @Test
        void whenPresentAndValid_thenPasses() {
            var rule = HeaderRule.builder().headerName("X-Regex").required(true)
                    .validator(new RegexValidator("^v\\d+$")).build();

            assertDoesNotThrow(() -> interceptor.validate(rule, "v1"));
        }
    }
}
