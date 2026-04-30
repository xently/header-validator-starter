package co.ke.xently.common.headers;

import co.ke.xently.common.headers.validators.DefaultHeaderValidator;
import co.ke.xently.common.headers.validators.ValidationResult;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class HeaderRuleTest {
    static Stream<Executable> shouldThrowExceptionForMutuallyExclusiveProperties() {
        return Stream.of(
                () -> {
                    var rule = new HeaderRule();
                    rule.setAllowedValues(Set.of("Example"));
                    rule.setValidator(new DefaultHeaderValidator());
                },
                () -> {
                    var rule = new HeaderRule();
                    rule.setValidator(new DefaultHeaderValidator());
                    rule.setAllowedValues(Set.of("Example"));
                }
        );
    }

    @ParameterizedTest
    @MethodSource
    void shouldThrowExceptionForMutuallyExclusiveProperties(Executable executable) {
        var actual = assertThrows(IllegalArgumentException.class, executable);

        assertThat(actual.getMessage())
                .startsWith("Cannot have both a `validator` and `allowedValues` set.");
    }

    @Nested
    class isValid {
        static Stream<TestCase> shouldValidateRequired() {
            return Stream.of(
                    new TestCase("", ValidationResult.Failure.class),
                    new TestCase(" ", ValidationResult.Failure.class),
                    new TestCase("e", ValidationResult.Failure.class),
                    new TestCase(" e ", ValidationResult.Failure.class),
                    new TestCase("Expected", ValidationResult.Success.class),
                    new TestCase("expectEd", ValidationResult.Success.class),
                    new TestCase("Expected  ", ValidationResult.Success.class),
                    new TestCase("alter native", ValidationResult.Failure.class),
                    new TestCase("alternative", ValidationResult.Success.class)
            );
        }

        static Stream<TestCase> shouldValidateOptional() {
            return Stream.of(
                    new TestCase("", ValidationResult.Failure.class),
                    new TestCase(" ", ValidationResult.Failure.class),
                    new TestCase("e", ValidationResult.Failure.class),
                    new TestCase(" e ", ValidationResult.Failure.class),
                    new TestCase("Expected", ValidationResult.Success.class),
                    new TestCase("expectEd", ValidationResult.Success.class),
                    new TestCase("Expected  ", ValidationResult.Success.class),
                    new TestCase("alter native", ValidationResult.Failure.class),
                    new TestCase("alternative", ValidationResult.Success.class)
            );
        }

        @ParameterizedTest
        @MethodSource
        void shouldValidateRequired(TestCase testCase) {
            var rule = HeaderRule.builder()
                    .headerName("X-Header-Name")
                    .allowedValues(Set.of("Expected", "Alternative"))
                    .build();

            var actual = rule.validate(testCase.headerValue());

            assertInstanceOf(testCase.expected(), actual);
        }

        @ParameterizedTest
        @MethodSource
        void shouldValidateOptional(TestCase testCase) {
            var rule = HeaderRule.builder()
                    .headerName("X-Header-Name")
                    .allowedValues(Set.of("Expected", "Alternative"))
                    .required(false)
                    .build();

            var actual = rule.validate(testCase.headerValue());

            assertInstanceOf(testCase.expected(), actual);
        }

        record TestCase(String headerValue, Class<? extends ValidationResult> expected) {
        }
    }

    @Nested
    class equals {
        static Stream<TestCase> shouldCorrectlyEvaluateEquals() {
            return Stream.of(
                    new TestCase(
                            HeaderRule.builder()
                                    .headerName("X-Header-Name")
                                    .build(),
                            HeaderRule.builder()
                                    .headerName("X-Header-Name")
                                    .build(),
                            true
                    ),
                    new TestCase(
                            HeaderRule.builder()
                                    .headerName("X-Header-Name")
                                    .build(),
                            null,
                            false
                    ),
                    new TestCase(
                            HeaderRule.builder()
                                    .headerName("X-Header-Name")
                                    .build(),
                            "",
                            false
                    ),
                    new TestCase(
                            HeaderRule.builder()
                                    .headerName("X-Header-Name")
                                    .build(),
                            HeaderRule.builder()
                                    .headerName("X-FeatureCode")
                                    .build(),
                            false
                    ),
                    new TestCase(
                            HeaderRule.builder()
                                    .headerName("X-FeatureCode")
                                    .build(),
                            HeaderRule.builder()
                                    .headerName("X-Header-Name")
                                    .build(),
                            false
                    ),
                    new TestCase(
                            HeaderRule.builder()
                                    .headerName("X-FeatureCode")
                                    .build(),
                            HeaderRule.builder()
                                    .headerName("X-FeatureCode")
                                    .build(),
                            true
                    ),
                    new TestCase(
                            HeaderRule.builder()
                                    .headerName("X-Featurecode")
                                    .build(),
                            HeaderRule.builder()
                                    .headerName("X-FeatureCode")
                                    .build(),
                            true
                    ),
                    new TestCase(
                            HeaderRule.builder()
                                    .headerName("X-Featurecode")
                                    .required(true)
                                    .build(),
                            HeaderRule.builder()
                                    .headerName("X-FeatureCode")
                                    .required(false)
                                    .build(),
                            true
                    ),
                    new TestCase(
                            HeaderRule.builder()
                                    .headerName("X-Featurecode")
                                    .required(true)
                                    .validator((headerName, headerValue) -> new ValidationResult.Success())
                                    .build(),
                            HeaderRule.builder()
                                    .headerName("X-FeatureCode")
                                    .required(false)
                                    .validator((headerName, headerValue) -> new ValidationResult.Failure("Failure"))
                                    .build(),
                            true
                    ),
                    new TestCase(
                            HeaderRule.builder()
                                    .headerName("X-Featurecode")
                                    .required(true)
                                    .validator((headerName, headerValue) -> new ValidationResult.Success())
                                    .build(),
                            HeaderRule.builder()
                                    .headerName("X-FeatureCode1")
                                    .required(false)
                                    .validator((headerName, headerValue) -> new ValidationResult.Failure("Failure"))
                                    .build(),
                            false
                    ),
                    new TestCase(
                            HeaderRule.builder()
                                    .headerName(" X-Featurecode")
                                    .required(true)
                                    .validator((headerName, headerValue) -> new ValidationResult.Success())
                                    .build(),
                            HeaderRule.builder()
                                    .headerName("X-FeatureCode  ")
                                    .required(false)
                                    .validator((headerName, headerValue) -> new ValidationResult.Failure("Failure"))
                                    .build(),
                            true
                    )
            );
        }

        @ParameterizedTest
        @MethodSource
        void shouldCorrectlyEvaluateEquals(TestCase testCase) {
            var actual = testCase.rule1.equals(testCase.rule2);

            assertEquals(testCase.expected(), actual);
        }

        record TestCase(HeaderRule rule1, Object rule2, boolean expected) {
        }
    }
}