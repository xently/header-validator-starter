package co.ke.xently.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import co.ke.xently.common.RequestPayloadConverterConfiguration.RequestPayloadJackson2Decoder;
import co.ke.xently.common.utils.converter.PayloadConverter;
import co.ke.xently.common.utils.dto.Request;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.ResolvableType;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.util.MimeType;
import reactor.core.publisher.Mono;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

class RequestConverterConfigurationTest {

    private final ObjectMapper mapper = new ObjectMapper();
    private final PayloadConverter payloadConverter = new PayloadConverter() {
    };

    @AfterEach
    void tearDown() {
        RequestContextHolder.clear();
    }

    @Test
    void shouldRegisterCustomJackson2Decoder() {
        var configurer = ServerCodecConfigurer.create();
        var config = new RequestPayloadConverterConfiguration(mapper, payloadConverter);

        config.configureHttpMessageCodecs(configurer);

        Jackson2JsonDecoder decoder = configurer.getReaders().stream()
                .filter(r -> r instanceof org.springframework.http.codec.DecoderHttpMessageReader<?> d
                        && d.getDecoder() instanceof Jackson2JsonDecoder)
                .map(r -> (org.springframework.http.codec.DecoderHttpMessageReader<?>) r)
                .map(org.springframework.http.codec.DecoderHttpMessageReader::getDecoder)
                .map(Jackson2JsonDecoder.class::cast)
                .findFirst()
                .orElse(null);

        assertThat(decoder)
                .isInstanceOf(RequestPayloadJackson2Decoder.class);
    }

    @Test
    void shouldSetContextWhenDecodingRequestPayload() {
        var decoder = new RequestPayloadJackson2Decoder(mapper, payloadConverter);
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
