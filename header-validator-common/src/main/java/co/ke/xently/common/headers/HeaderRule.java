package co.ke.xently.common.headers;

import co.ke.xently.common.headers.validators.DefaultHeaderValidator;
import co.ke.xently.common.headers.validators.HeaderValidator;
import co.ke.xently.common.headers.validators.ValidationResult;
import lombok.*;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Locale;
import java.util.Objects;
import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HeaderRule {
    @NonNull
    private String headerName;
    @Builder.Default
    private boolean required = true;
    @Nullable
    @Builder.Default
    private HeaderValidator validator = null;
    @Nullable
    @Builder.Default
    private Set<String> allowedValues = Set.of();

    private void ensureCompliance() {
        boolean hasValidator = validator != null;
        boolean hasAllowedValues = allowedValues != null && !allowedValues.isEmpty();
        if (hasValidator && hasAllowedValues) {
            throw new IllegalArgumentException("Cannot have both a `validator` and `allowedValues` set. Prefer `allowedValues` if the header '%s' value should be checked against specific set of options using the default validator, use a custom `validator` if the default validator is not sufficient.".formatted(getHeaderName()));
        }
    }

    public @NonNull Set<String> getAllowedValues() {
        return Objects.requireNonNullElse(allowedValues, Set.of());
    }

    public void setValidator(@Nullable HeaderValidator validator) {
        this.validator = validator;
        ensureCompliance();
    }

    public void setAllowedValues(@Nullable Set<String> allowedValues) {
        this.allowedValues = allowedValues;
        ensureCompliance();
    }

    public @NonNull HeaderValidator getValidator() {
        if (validator != null) return validator;

        return new DefaultHeaderValidator(getAllowedValues());
    }

    @NonNull
    public ValidationResult validate(@NonNull String headerValue) {
        return getValidator().validate(headerName, headerValue);
    }

    @NonNull
    private String getCaseInsensitiveHeaderName() {
        String name = getHeaderName();
        return name.strip().toLowerCase(Locale.ROOT);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        HeaderRule that = (HeaderRule) o;

        String name = getCaseInsensitiveHeaderName();

        return name.equals(that.getCaseInsensitiveHeaderName());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getCaseInsensitiveHeaderName());
    }

    @Override
    public String toString() {
        return "HeaderRule{headerName='%s', required=%s}".formatted(headerName, required);
    }
}