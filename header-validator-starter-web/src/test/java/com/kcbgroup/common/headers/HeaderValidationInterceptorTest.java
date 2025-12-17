package com.kcbgroup.common.headers;

import com.kcbgroup.common.headers.exceptions.HeadersValidationException;
import com.kcbgroup.common.headers.exceptions.InvalidHeaderValueException;
import com.kcbgroup.common.headers.exceptions.MissingHeaderException;
import com.kcbgroup.common.headers.validators.RegexValidator;
import com.kcbgroup.common.headers.validators.ValidationResult;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class HeaderValidationInterceptorTest {

    private static MockHttpServletRequest getMockHttpServletRequest() {
        var request = new MockHttpServletRequest();
        request.addHeader("X-FeatureCode", "101");
        request.addHeader("X-FeatureName", "BPM");
        request.addHeader("X-ServiceCode", "10001");
        request.addHeader("X-ServiceName", "ke-kcb-salesforce-bpm-st-v1.0.0");
        request.addHeader("X-ChannelCode", "10");
        request.addHeader("X-ChannelCategory", "102");
        request.addHeader("X-ChannelName", "App");
        request.addHeader("X-RouteCode", "SFA");
        request.addHeader("X-TimeStamp", "1750844604");
        request.addHeader("X-ServiceMode", "NA");
        request.addHeader("X-SubscriberEvents", "NA");
        request.addHeader("X-CallBackURL", "https://example.com/callback");
        request.addHeader("X-ServiceSubCategory", "upload-document");
        request.addHeader("X-MinorServiceVersion", "1.0");
        return request;
    }

    @Test
    void whenNoProperties_thenPreHandlePasses() {
        var props = new HeaderValidationProperties(Set.of());
        var interceptor = new HeaderValidationInterceptor(props);

        var request = getMockHttpServletRequest();

        boolean actual = interceptor.preHandle(request, new MockHttpServletResponse(), new Object());

        assertThat(actual)
                .isTrue();
    }

    @Test
    void whenValidHeaders_thenPreHandleReturnsTrue() {
        var rule = HeaderRule.builder()
                .headerName("X-Custom")
                .validator((n, v) -> v.equals("OK")
                        ? new ValidationResult.Success()
                        : new ValidationResult.Failure("Invalid value"))
                .build();
        var props = new HeaderValidationProperties(Set.of(rule));
        var interceptor = new HeaderValidationInterceptor(props);

        var request = getMockHttpServletRequest();
        request.addHeader("X-Custom", "OK");

        boolean actual = interceptor.preHandle(request, new MockHttpServletResponse(), new Object());

        assertThat(actual)
                .isTrue();
    }

    @Test
    void whenMissingRequired_thenThrowsAggregatedException() {
        var required = HeaderRule.builder().headerName("X-Req").required(true).build();
        var invalid = HeaderRule.builder().headerName("X-Invalid").required(true)
                .validator(new RegexValidator("^v\\d+$")).build();

        var props = new HeaderValidationProperties(Set.of(required, invalid));
        var interceptor = new HeaderValidationInterceptor(props);

        var request = getMockHttpServletRequest();
        request.addHeader("X-Invalid", "bad");

        var ex = assertThrows(HeadersValidationException.class,
                () -> interceptor.preHandle(request, new MockHttpServletResponse(), new Object()));

        assertAll(
                () -> assertThat(ex.getHeaderExceptions().size())
                        .isEqualTo(2),
                () -> {
                    var headerName = ex.getHeaderExceptions()
                            .stream()
                            .filter(e -> e instanceof MissingHeaderException)
                            .findFirst()
                            .orElseThrow()
                            .getRule()
                            .getHeaderName();
                    assertThat(headerName)
                            .isEqualTo("X-Req");
                },
                () -> {
                    var headerName = ex.getHeaderExceptions()
                            .stream()
                            .filter(e -> e instanceof InvalidHeaderValueException)
                            .findFirst()
                            .orElseThrow()
                            .getRule()
                            .getHeaderName();
                    assertThat(headerName)
                            .isEqualTo("X-Invalid");
                }
        );
    }
}