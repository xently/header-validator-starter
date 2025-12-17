package com.kcbgroup.common;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class KCBRequestFilter implements WebFilter {
    @Override
    @NonNull
    public Mono<Void> filter(@NonNull ServerWebExchange exchange, @NonNull WebFilterChain chain) {
        var context = new KCBRequestContext(UUID.randomUUID().toString());
        KCBRequestContextHolder.setContext(context);
        return chain.filter(exchange)
                .doFinally(signal -> KCBRequestContextHolder.clear());
    }
}
