package co.ke.xently.common;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RequestContextHolderTest {

    @Test
    void shouldSetGetAndClearBehavior() {
        assertThat(RequestContextHolder.getContext())
                .isNull();

        var ctx = new RequestContext("conv-123");
        RequestContextHolder.setContext(ctx);
        assertThat(RequestContextHolder.getContext().conversationID())
                .isEqualTo("conv-123");

        RequestContextHolder.clear();
        assertThat(RequestContextHolder.getContext())
                .isNull();
    }
}
