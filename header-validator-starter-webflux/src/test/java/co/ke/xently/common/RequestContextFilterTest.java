package co.ke.xently.common;

import org.junit.jupiter.api.Test;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class RequestContextFilterTest {

    @Test
    void shouldSetAndClearRequestContext() {
        var filter = new RequestContextFilter();
        var exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/test").build());

        var seen = new AtomicReference<RequestContext>();

        WebFilterChain chain = ex -> Mono.fromRunnable(() -> seen.set(RequestContextHolder.getContext()));

        filter.filter(exchange, chain).block();

        assertNotNull(seen.get(), "context should be set during request processing");
        assertNull(RequestContextHolder.getContext(), "context should be cleared after processing");
    }
}
