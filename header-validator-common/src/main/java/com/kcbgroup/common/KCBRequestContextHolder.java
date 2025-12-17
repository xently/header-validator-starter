package com.kcbgroup.common;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class KCBRequestContextHolder {
    private static final ThreadLocal<KCBRequestContext> CONTEXT = new ThreadLocal<>();

    private KCBRequestContextHolder() {
    }

    public static KCBRequestContext getContext() {
        log.debug("Getting request context...");
        return CONTEXT.get();
    }

    public static void setContext(KCBRequestContext context) {
        log.debug("Setting request context: {}", context);
        CONTEXT.set(context);
    }

    public static void clear() {
        log.debug("Clearing request context...");
        CONTEXT.remove();
    }
}
