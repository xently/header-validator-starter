package co.ke.xently.common;

import co.ke.xently.common.utils.converter.PayloadConverter;
import co.ke.xently.common.utils.dto.Request;
import lombok.AllArgsConstructor;
import org.reactivestreams.Publisher;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.jackson.autoconfigure.JacksonAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.core.ResolvableType;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.http.codec.json.JacksonJsonDecoder;
import org.jspecify.annotations.NonNull;
import org.springframework.util.MimeType;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tools.jackson.databind.json.JsonMapper;

import java.util.Map;
import java.util.UUID;

@AutoConfiguration
@AllArgsConstructor
@ConditionalOnClass(JsonMapper.class)
@AutoConfigureAfter(JacksonAutoConfiguration.class)
@Import({RequestContextFilter.class, HeaderValidatorExceptionHandler.class})
class RequestPayloadConverterConfiguration implements WebFluxConfigurer {
    private final JsonMapper objectMapper;
    private final PayloadConverter payloadConverter;

    @Override
    public void configureHttpMessageCodecs(@NonNull ServerCodecConfigurer configurer) {
        var decoder = new RequestPayloadJacksonDecoder(objectMapper, payloadConverter);
        configurer.defaultCodecs().jacksonJsonDecoder(decoder);
    }

    static class RequestPayloadJacksonDecoder extends JacksonJsonDecoder {
        private final PayloadConverter converter;

        RequestPayloadJacksonDecoder(JsonMapper mapper, PayloadConverter converter) {
            super(mapper);
            this.converter = converter;
        }

        @NonNull
        @Override
        public Flux<Object> decode(@NonNull Publisher<DataBuffer> input,
                                   @NonNull ResolvableType elementType,
                                   MimeType mimeType,
                                   Map<String, Object> hints) {
            return super.decode(input, elementType, mimeType, hints)
                    .doOnNext(this::maybeSetContext);
        }

        @NonNull
        @Override
        public Mono<Object> decodeToMono(@NonNull Publisher<DataBuffer> input,
                                         @NonNull ResolvableType elementType,
                                         MimeType mimeType,
                                         Map<String, Object> hints) {
            return super.decodeToMono(input, elementType, mimeType, hints)
                    .doOnNext(this::maybeSetContext);
        }

        private void maybeSetContext(Object payload) {
            if (converter.convertToRequestPayload(payload) instanceof Request request) {
                var requestContext = RequestContextHolder.getContext();
                if (requestContext == null) {
                    requestContext = new RequestContext(UUID.randomUUID().toString(), request.messageID());
                } else {
                    requestContext = new RequestContext(requestContext.conversationID(), request.messageID());
                }
                RequestContextHolder.setContext(requestContext);
            }
        }
    }
}
