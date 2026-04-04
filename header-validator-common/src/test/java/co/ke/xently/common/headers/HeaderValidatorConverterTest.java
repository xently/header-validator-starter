package co.ke.xently.common.headers;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import co.ke.xently.common.headers.validators.DefaultHeaderValidator;
import co.ke.xently.common.headers.validators.EpochTimestampValidator;
import co.ke.xently.common.headers.validators.HeaderValidator;
import co.ke.xently.common.headers.validators.ValidationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.jspecify.annotations.NonNull;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HeaderValidatorConverterTest {
    private ListAppender<ILoggingEvent> loggingEvents;

    @BeforeEach
    public void setup() {
        loggingEvents = new ListAppender<>();
        loggingEvents.start();
        Logger logger = (Logger) LoggerFactory.getLogger(HeaderValidatorConverter.class);
        logger.addAppender(loggingEvents);
    }

    static Stream<TestCase> shouldReturnDefaultHeaderValidator() {
        return Stream.of(
                new TestCase(
                        "com.example.DoesNotExistValidator",
                        HeaderValidatorSource.FQCN,
                        "Failed to create an instance of"),
                new TestCase(
                        (PrivateConstructorValidator.class).getName(),
                        HeaderValidatorSource.FQCN,
                        "Failed to create an instance of"),
                new TestCase(
                        (NoNoArgConstructorValidator.class).getName(),
                        HeaderValidatorSource.FQCN,
                        "Failed to create an instance of"),
                new TestCase(
                        AbstractValidator.class.getName(),
                        HeaderValidatorSource.FQCN,
                        "Failed to create an instance of"),
                new TestCase(
                        (ThrowingCtorValidator.class).getName(),
                        HeaderValidatorSource.FQCN,
                        "Failed to create an instance of"),
                new TestCase(
                        "com.example.DoesNotExistValidator",
                        HeaderValidatorSource.FQCNB4BeanDefinition,
                        "Bean definition retrieval failed for instance of type"),
                new TestCase(
                        (PrivateConstructorValidator.class).getName(),
                        HeaderValidatorSource.FQCNB4BeanDefinition,
                        "Bean definition retrieval failed for instance of type"),
                new TestCase(
                        (NoNoArgConstructorValidator.class).getName(),
                        HeaderValidatorSource.FQCNB4BeanDefinition,
                        "Bean definition retrieval failed for instance of type"),
                new TestCase(
                        AbstractValidator.class.getName(),
                        HeaderValidatorSource.FQCNB4BeanDefinition,
                        "Bean definition retrieval failed for instance of type"),
                new TestCase(
                        (ThrowingCtorValidator.class).getName(),
                        HeaderValidatorSource.FQCNB4BeanDefinition,
                        "Bean definition retrieval failed for instance of type"),
                new TestCase(
                        "com.example.DoesNotExistValidator",
                        HeaderValidatorSource.BeanDefinition,
                        "Bean definition retrieval failed for instance of type"),
                new TestCase(
                        (PrivateConstructorValidator.class).getName(),
                        HeaderValidatorSource.BeanDefinition,
                        "Bean definition retrieval failed for instance of type"),
                new TestCase(
                        (NoNoArgConstructorValidator.class).getName(),
                        HeaderValidatorSource.BeanDefinition,
                        "Bean definition retrieval failed for instance of type"),
                new TestCase(
                        AbstractValidator.class.getName(),
                        HeaderValidatorSource.BeanDefinition,
                        "Bean definition retrieval failed for instance of type"),
                new TestCase(
                        (ThrowingCtorValidator.class).getName(),
                        HeaderValidatorSource.BeanDefinition,
                        "Bean definition retrieval failed for instance of type"),
                new TestCase(
                        "com.example.DoesNotExistValidator",
                        HeaderValidatorSource.BeanDefinitionB4FQCN,
                        "Failed to create an instance of"),
                new TestCase(
                        (PrivateConstructorValidator.class).getName(),
                        HeaderValidatorSource.BeanDefinitionB4FQCN,
                        "Failed to create an instance of"),
                new TestCase(
                        (NoNoArgConstructorValidator.class).getName(),
                        HeaderValidatorSource.BeanDefinitionB4FQCN,
                        "Failed to create an instance of"),
                new TestCase(
                        AbstractValidator.class.getName(),
                        HeaderValidatorSource.BeanDefinitionB4FQCN,
                        "Failed to create an instance of"),
                new TestCase(
                        (ThrowingCtorValidator.class).getName(),
                        HeaderValidatorSource.BeanDefinitionB4FQCN,
                        "Failed to create an instance of")
        );
    }

    @Test
    void shouldCreateInstanceFromFQCN() {
        var context = mock(ApplicationContext.class);
        var environment = mock(Environment.class);
        when(context.getEnvironment())
                .thenReturn(environment);
        when(
                environment.getProperty(
                        "xently.api.headers.validator.source",
                        HeaderValidatorSource.class,
                        HeaderValidatorSource.FQCNB4BeanDefinition
                )
        ).thenReturn(HeaderValidatorSource.FQCNB4BeanDefinition);
        var converter = new HeaderValidatorConverter(context);

        var actual = converter.convert(EpochTimestampValidator.class.getName());

        assertAll(
                () -> assertThat(actual)
                        .withFailMessage("Expected non-null instance when a valid FQCN is provided")
                        .isNotNull(),
                () -> assertThat(actual)
                        .isInstanceOf(EpochTimestampValidator.class)
        );
    }

    @ParameterizedTest
    @MethodSource
    void shouldReturnDefaultHeaderValidator(TestCase testCase) {
        var context = mock(ApplicationContext.class);
        var environment = mock(Environment.class);
        when(context.getEnvironment())
                .thenReturn(environment);
        when(
                environment.getProperty(
                        "xently.api.headers.validator.source",
                        HeaderValidatorSource.class,
                        HeaderValidatorSource.FQCNB4BeanDefinition
                )
        ).thenReturn(testCase.validatorSource());
        when(context.getBean(testCase.className(), HeaderValidator.class))
                .thenThrow(new NoSuchBeanDefinitionException("Error"));
        var converter = new HeaderValidatorConverter(context);

        var actual = converter.convert(testCase.className());

        var errorLogMessages = loggingEvents.list
                .stream()
                .filter(e -> e.getLevel().equals(Level.ERROR))
                .map(ILoggingEvent::getFormattedMessage)
                .toList();

        assertAll(
                () -> assertThat(actual)
                        .isInstanceOf(DefaultHeaderValidator.class),
                () -> assertThat(errorLogMessages)
                        .containsOnly("%s '%s' from '%s'." .formatted(testCase.expectedErrorMessagePrefix(), HeaderValidator.class.getName(), testCase.className()))
        );
    }

    public static class NoNoArgConstructorValidator implements HeaderValidator {
        public NoNoArgConstructorValidator(String ignoredAny) {
        }

        @Override
        @NonNull
        public ValidationResult validate(@NonNull String headerName, @NonNull String headerValue) {
            return new ValidationResult.Success();
        }
    }

    public static class ThrowingCtorValidator implements HeaderValidator {
        public ThrowingCtorValidator() {
            throw new RuntimeException();
        }

        @Override
        @NonNull
        public ValidationResult validate(@NonNull String headerName, @NonNull String headerValue) {
            return new ValidationResult.Success();
        }
    }

    public static abstract class AbstractValidator implements HeaderValidator {
    }

    public static class PrivateConstructorValidator implements HeaderValidator {
        private PrivateConstructorValidator() {
        }

        @Override
        @NonNull
        public ValidationResult validate(@NonNull String headerName, @NonNull String headerValue) {
            return new ValidationResult.Success();
        }
    }

    record TestCase(String className, HeaderValidatorSource validatorSource, String expectedErrorMessagePrefix) {

    }
}
