package co.ke.xently.common.headers;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
final class HeaderValidationWebFilter extends AbstractHeaderValidator implements WebFilter {
    public HeaderValidationWebFilter(HeaderValidationProperties properties) {
        super(properties);
    }

    @Override
    @NonNull
    public Mono<Void> filter(@NonNull ServerWebExchange exchange, @NonNull WebFilterChain chain) {
        var exception = createHeadersValidationException(headerName -> exchange.getRequest().getHeaders().getFirst(headerName));

        if (!exception.getHeaderExceptions().isEmpty()) {
            return Mono.error(exception);
        }

        return chain.filter(exchange);
    }
}
