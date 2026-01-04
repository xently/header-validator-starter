package co.ke.xently.common.headers;

import co.ke.xently.common.headers.validators.DefaultHeaderValidator;
import co.ke.xently.common.headers.validators.EpochTimestampValidator;
import co.ke.xently.common.headers.validators.ValidationResult;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class HeaderValidationPropertiesTest {

    @Test
    void getHeaders_containsDefaults() {
        var props = new HeaderValidationProperties(Set.of());

        var actual = props.headers();

        assertAll(
                () -> assertThat(actual)
                        .isNotEmpty(),
                () -> assertThat(actual)
                        .contains(HeaderRule.builder().headerName("X-FeatureCode").build()),
                () -> assertThat(actual)
                        .contains(HeaderRule.builder().headerName("X-FeatureName").build()),
                () -> assertThat(actual)
                        .contains(HeaderRule.builder().headerName("X-ChannelCode").build()),
                () -> assertThat(actual)
                        .contains(HeaderRule.builder().headerName("X-CallBackURL").build())
        );
    }

    @Test
    void setHeaders_thenGetHeaders_mergesWithDefaults() {
        var custom = HeaderRule.builder().headerName("X-Custom").required(false).build();
        var props = new HeaderValidationProperties(Set.of(custom));

        var actual = props.headers();

        assertAll(
                () -> assertThat(actual)
                        .contains(custom),
                () -> assertThat(actual)
                        .contains(HeaderRule.builder().headerName("X-FeatureName").build())
        );
    }

    @Nested
    class PropertiesLoading {
        private final ApplicationContextRunner runner = new ApplicationContextRunner()
                .withBean(HeaderValidatorConverter.class)
                .withUserConfiguration(TestConfig.class);

        private static Stream<TestCase> shouldDefaultToRequiredAndDefaultValidator() {
            return Stream.of(
                    new TestCase("", ValidationResult.Failure.class),
                    new TestCase("         ", ValidationResult.Failure.class),
                    new TestCase("   d ", ValidationResult.Success.class)
            );
        }

        @Test
        void shouldBindPropertiesCorrectly() {
            runner.withPropertyValues(
                    "xently.api.headers.validation.headers.[0].header-name=X-Custom-Header",
                    "xently.api.headers.validation.headers.[0].required=true",
                    "xently.api.headers.validation.headers.[0].validator=co.ke.xently.common.headers.validators.EpochTimestampValidator"
            ).run(context -> {
                assertThat(context)
                        .hasSingleBean(HeaderValidationProperties.class);

                var properties = context.getBean(HeaderValidationProperties.class);
                var customHeader = properties.headers()
                        .stream()
                        .filter(headerRule -> headerRule.getHeaderName().equals("X-Custom-Header"))
                        .findFirst()
                        .orElseThrow();

                assertAll(
                        () -> assertThat(customHeader.getHeaderName())
                                .isEqualTo("X-Custom-Header"),
                        () -> assertThat(customHeader.isRequired())
                                .isTrue(),
                        () -> assertThat(customHeader.getValidator())
                                .isInstanceOf(EpochTimestampValidator.class)
                );
            });
        }

        @ParameterizedTest
        @MethodSource
        void shouldDefaultToRequiredAndDefaultValidator(TestCase testCase) {
            runner.withPropertyValues("xently.api.headers.validation.headers.[0].header-name=X-Custom-Header").run(context -> {
                assertThat(context)
                        .hasSingleBean(HeaderValidationProperties.class);

                var properties = context.getBean(HeaderValidationProperties.class);
                var customHeader = properties.headers()
                        .stream()
                        .filter(headerRule -> headerRule.getHeaderName().equals("X-Custom-Header"))
                        .findFirst()
                        .orElseThrow();

                assertAll(
                        () -> assertThat(customHeader.getHeaderName())
                                .isEqualTo("X-Custom-Header"),
                        () -> assertThat(customHeader.isRequired())
                                .isTrue(),
                        () -> assertThat(customHeader.getValidator())
                                .isInstanceOf(DefaultHeaderValidator.class),
                        () -> assertThat(customHeader.validate(testCase.headerValue()))
                                .isInstanceOf(testCase.expectedClass())
                );
            });
        }

        @EnableConfigurationProperties({HeaderValidationProperties.class})
        private static class TestConfig {
        }

        private record TestCase(String headerValue, Class<? extends ValidationResult> expectedClass) {
        }
    }
}
