package com.kcbgroup.common.headers.validators;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class EpochTimestampValidatorTest {
    static Stream<TestCase> timestampProvider() {
        return Stream.of(
                new TestCase("", ValidationResult.Failure.class),
                new TestCase("1nvalid", ValidationResult.Failure.class),
                new TestCase("1", ValidationResult.Success.class),
                new TestCase(String.valueOf(Long.MIN_VALUE), ValidationResult.Success.class),
                new TestCase(String.valueOf(Long.MAX_VALUE), ValidationResult.Success.class),
                new TestCase(String.valueOf(System.currentTimeMillis()), ValidationResult.Success.class)
        );
    }

    @ParameterizedTest
    @MethodSource("timestampProvider")
    void shouldValidate(TestCase timestamp) {
        var validator = new EpochTimestampValidator();

        var actual = validator.validate("X-Header-Name", timestamp.headerValue());

        assertInstanceOf(timestamp.expected(), actual);
    }

    record TestCase(String headerValue, Class<? extends ValidationResult> expected) {
    }
}