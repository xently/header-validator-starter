package co.ke.xently.common.headers;

import co.ke.xently.common.headers.exceptions.HeaderException;
import co.ke.xently.common.headers.exceptions.HeadersValidationException;
import co.ke.xently.common.headers.exceptions.InvalidHeaderValueException;
import co.ke.xently.common.headers.exceptions.MissingHeaderException;
import co.ke.xently.common.headers.validators.ValidationResult;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.springframework.lang.NonNull;

import java.util.function.Function;

@AllArgsConstructor(access = AccessLevel.PROTECTED)
abstract class AbstractHeaderValidator {
    @NonNull
    protected final HeaderValidationProperties properties;

    protected HeadersValidationException createHeadersValidationException(
            @NonNull Function<String, String> headerValue) {
        var exception = new HeadersValidationException();

        for (var rule : properties.headers()) {
            var value = headerValue.apply(rule.getHeaderName());
            try {
                validate(rule, value);
            } catch (HeaderException e) {
                exception.addHeaderException(e);
            }
        }
        return exception;
    }

    protected void validate(@NonNull HeaderRule rule, String headerValue) throws HeaderException {
        if (headerValue == null) {
            if (!rule.isRequired()) return;

            throw new MissingHeaderException(rule);
        }

        switch (rule.validate(headerValue)) {
            case ValidationResult.Success ignored -> {
            }
            case ValidationResult.Failure failure -> throw new InvalidHeaderValueException(
                    rule,
                    failure
            );
        }
    }
}
