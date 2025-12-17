package com.kcbgroup.common;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class KCBRequestContextHolderTest {

    @Test
    void shouldSetGetAndClearBehavior() {
        assertThat(KCBRequestContextHolder.getContext())
                .isNull();

        var ctx = new KCBRequestContext("conv-123");
        KCBRequestContextHolder.setContext(ctx);
        assertThat(KCBRequestContextHolder.getContext().conversationID())
                .isEqualTo("conv-123");

        KCBRequestContextHolder.clear();
        assertThat(KCBRequestContextHolder.getContext())
                .isNull();
    }
}
