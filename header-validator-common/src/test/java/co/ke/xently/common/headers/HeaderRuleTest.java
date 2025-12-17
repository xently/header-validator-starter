package co.ke.xently.common.headers;

import co.ke.xently.common.headers.validators.ValidationResult;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class HeaderRuleTest {
    @Nested
    class isValid {
        static Stream<TestCase> shouldValidateRequired() {
            return Stream.of(
                    new TestCase("", ValidationResult.Failure.class),
                    new TestCase(" ", ValidationResult.Failure.class),
                    new TestCase("e", ValidationResult.Success.class),
                    new TestCase(" e ", ValidationResult.Success.class)
            );
        }

        static Stream<TestCase> shouldValidateOptional() {
            return Stream.of(
                    new TestCase("", ValidationResult.Failure.class),
                    new TestCase(" ", ValidationResult.Failure.class),
                    new TestCase("e", ValidationResult.Success.class),
                    new TestCase(" e ", ValidationResult.Success.class)
            );
        }

        @ParameterizedTest
        @MethodSource
        void shouldValidateRequired(TestCase testCase) {
            var rule = new HeaderRule();

            var actual = rule.validate(testCase.headerValue());

            assertInstanceOf(testCase.expected(), actual);
        }

        @ParameterizedTest
        @MethodSource
        void shouldValidateOptional(TestCase testCase) {
            var rule = HeaderRule.builder()
                    .headerName("X-Header-Name")
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