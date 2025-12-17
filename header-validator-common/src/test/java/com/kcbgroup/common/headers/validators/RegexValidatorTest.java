package com.kcbgroup.common.headers.validators;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class RegexValidatorTest {
    static Stream<TestCase> regexProvider() {
        return Stream.of(
                new TestCase("", ValidationResult.Failure.class),
                new TestCase("1nvalid", ValidationResult.Failure.class),
                new TestCase("v", ValidationResult.Failure.class),
                new TestCase("v1", ValidationResult.Success.class),
                new TestCase("v1.", ValidationResult.Failure.class),
                new TestCase("v1.12", ValidationResult.Success.class),
                new TestCase("v1.123.3", ValidationResult.Success.class),
                new TestCase("1.123.3", ValidationResult.Failure.class),
                new TestCase("v1.123.3-alpha01", ValidationResult.Failure.class),
                new TestCase("1.123.3-alpha01", ValidationResult.Failure.class)
        );
    }

    @ParameterizedTest
    @MethodSource("regexProvider")
    void shouldValidate(TestCase testCase) {
        var validator = new RegexValidator("v\\d+(.\\d+){0,2}");

        var actual = validator.validate("X-Header-Name", testCase.headerValue());

        assertInstanceOf(testCase.expected(), actual);
    }

    record TestCase(String headerValue, Class<? extends ValidationResult> expected) {
    }
}