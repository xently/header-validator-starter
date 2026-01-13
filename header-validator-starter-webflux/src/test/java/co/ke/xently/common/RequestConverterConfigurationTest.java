package co.ke.xently.common;

import co.ke.xently.common.RequestPayloadConverterConfiguration.RequestPayloadJacksonDecoder;
import co.ke.xently.common.utils.converter.PayloadConverter;
import co.ke.xently.common.utils.dto.Request;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.ResolvableType;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.codec.DecoderHttpMessageReader;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.http.codec.json.JacksonJsonDecoder;
import org.springframework.util.MimeType;
import reactor.core.publisher.Mono;
import tools.jackson.databind.json.JsonMapper;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

class RequestConverterConfigurationTest {

    private final JsonMapper mapper = new JsonMapper();
    private final PayloadConverter payloadConverter = new PayloadConverter() {
    };

    @AfterEach
    void tearDown() {
        RequestContextHolder.clear();
    }

    @Test
    void shouldRegisterCustomJacksonDecoder() {
        var configurer = ServerCodecConfigurer.create();
        var config = new RequestPayloadConverterConfiguration(mapper, payloadConverter);

        config.configureHttpMessageCodecs(configurer);

        JacksonJsonDecoder decoder = configurer.getReaders().stream()
                .filter(r -> r instanceof DecoderHttpMessageReader<?> d
                        && d.getDecoder() instanceof JacksonJsonDecoder)
                .map(r -> (DecoderHttpMessageReader<?>) r)
                .map(DecoderHttpMessageReader::getDecoder)
                .map(JacksonJsonDecoder.class::cast)
                .findFirst()
                .orElse(null);

        assertThat(decoder)
                .isInstanceOf(RequestPayloadJacksonDecoder.class);
    }

    @Test
    void shouldSetContextWhenDecodingRequestPayload() {
        var decoder = new RequestPayloadJacksonDecoder(mapper, payloadConverter);
        // language=JSON
        var json = """
                {
                  "messageID": "mid-1"
                }""";
        var buffer = new DefaultDataBufferFactory().wrap(json.getBytes());

        record RequestPayload(String messageID) implements Request {}

        var result = decoder.decodeToMono(
                Mono.just(buffer),
                ResolvableType.forClass(RequestPayload.class),
                MimeType.valueOf("application/json"),
                Collections.emptyMap()
        ).block();

        assertThat(result).isInstanceOf(RequestPayload.class);
        var ctx = RequestContextHolder.getContext();
        assertThat(ctx).isNotNull();
        assertThat(ctx.messageID()).isEqualTo("mid-1");
    }
}
