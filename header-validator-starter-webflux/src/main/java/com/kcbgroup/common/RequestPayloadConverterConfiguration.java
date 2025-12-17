package com.kcbgroup.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kcbgroup.common.utils.converter.PayloadConverter;
import com.kcbgroup.common.utils.dto.Request;
import lombok.AllArgsConstructor;
import org.reactivestreams.Publisher;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.core.ResolvableType;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.lang.NonNull;
import org.springframework.util.MimeType;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.UUID;

@AutoConfiguration
@AllArgsConstructor
@ConditionalOnClass(ObjectMapper.class)
@AutoConfigureAfter(JacksonAutoConfiguration.class)
@Import({KCBRequestFilter.class, HeaderValidatorExceptionHandler.class})
class RequestPayloadConverterConfiguration implements WebFluxConfigurer {
    private final ObjectMapper objectMapper;
    private final PayloadConverter payloadConverter;

    @Override
    public void configureHttpMessageCodecs(@NonNull ServerCodecConfigurer configurer) {
        var decoder = new RequestPayloadJackson2Decoder(objectMapper, payloadConverter);
        configurer.defaultCodecs().jackson2JsonDecoder(decoder);
    }

    static class RequestPayloadJackson2Decoder extends Jackson2JsonDecoder {
        private final PayloadConverter converter;

        RequestPayloadJackson2Decoder(ObjectMapper mapper, PayloadConverter converter) {
            super(
                    mapper,
                    MediaType.APPLICATION_JSON,
                    MediaType.APPLICATION_NDJSON,
                    MediaType.parseMediaType("application/*+json")
            );
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
                var requestContext = KCBRequestContextHolder.getContext();
                if (requestContext == null) {
                    requestContext = new KCBRequestContext(UUID.randomUUID().toString(), request.messageID());
                } else {
                    requestContext = new KCBRequestContext(requestContext.conversationID(), request.messageID());
                }
                KCBRequestContextHolder.setContext(requestContext);
            }
        }
    }
}
