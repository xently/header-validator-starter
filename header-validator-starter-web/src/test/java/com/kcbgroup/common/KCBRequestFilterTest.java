package com.kcbgroup.common;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class KCBRequestFilterTest {

    @Test
    void contextIsSetForChainAndClearedAfterwards() throws Exception {
        var request = new MockHttpServletRequest("GET", "/any");
        var response = new MockHttpServletResponse();

        FilterChain chain = (req, res) -> {
            var context = KCBRequestContextHolder.getContext();
            assertThat(context.conversationID())
                    .isNotNull();
            ((HttpServletResponse) res).setStatus(200);
        };

        var filter = new KCBRequestFilter();
        filter.doFilter(request, response, chain);

        assertAll(
                () -> assertThat(response.getStatus())
                        .isEqualTo(200),
                () -> {
                    // After filter returns, context should be cleared
                    assertThat(KCBRequestContextHolder.getContext())
                            .isNull();
                }
        );
    }
}
