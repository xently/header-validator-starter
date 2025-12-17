package com.kcbgroup.common.headers;

import com.kcbgroup.common.headers.validators.DefaultHeaderValidator;
import com.kcbgroup.common.headers.validators.HeaderValidator;
import com.kcbgroup.common.headers.validators.ValidationResult;
import lombok.*;
import org.springframework.lang.NonNull;

import java.util.Locale;
import java.util.Objects;

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
    @NonNull
    @Builder.Default
    private HeaderValidator validator = new DefaultHeaderValidator();

    @NonNull
    public ValidationResult validate(@NonNull String headerValue) {
        return validator.validate(headerName, headerValue);
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