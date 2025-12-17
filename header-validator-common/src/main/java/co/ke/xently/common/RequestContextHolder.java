package co.ke.xently.common;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class RequestContextHolder {
    private static final ThreadLocal<RequestContext> CONTEXT = new ThreadLocal<>();

    private RequestContextHolder() {
    }

    public static RequestContext getContext() {
        log.debug("Getting request context...");
        return CONTEXT.get();
    }

    public static void setContext(RequestContext context) {
        log.debug("Setting request context: {}", context);
        CONTEXT.set(context);
    }

    public static void clear() {
        log.debug("Clearing request context...");
        CONTEXT.remove();
    }
}
