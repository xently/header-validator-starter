package co.ke.xently.common.headers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.web.servlet.HandlerInterceptor;

final class HeaderValidationInterceptor extends AbstractHeaderValidator implements HandlerInterceptor {
    HeaderValidationInterceptor(@NonNull HeaderValidationProperties properties) {
        super(properties);
    }

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) {
        var exception = createHeadersValidationException(request::getHeader);
        if (!exception.getHeaderExceptions().isEmpty()) throw exception;
        return true;
    }
}