package co.ke.xently.common;

import co.ke.xently.common.utils.converter.PayloadConverter;
import co.ke.xently.common.utils.dto.Request;
import lombok.AllArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.jackson.autoconfigure.JacksonAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.core.ResolvableType;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverters;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@AutoConfiguration
@AllArgsConstructor
@ConditionalOnClass(JsonMapper.class)
@AutoConfigureAfter(JacksonAutoConfiguration.class)
@Import({RequestContextFilter.class, HeaderValidatorExceptionHandler.class})
class RequestPayloadConverterConfiguration implements WebMvcConfigurer {
    private final JsonMapper objectMapper;
    private final PayloadConverter payloadConverter;

    @Override
    public void configureMessageConverters(HttpMessageConverters.ServerBuilder builder) {
        builder.addCustomConverter(new RequestPayloadJacksonConverter(objectMapper, payloadConverter));
    }

    static class RequestPayloadJacksonConverter extends JacksonJsonHttpMessageConverter {
        private final PayloadConverter converter;

        RequestPayloadJacksonConverter(JsonMapper mapper, PayloadConverter converter) {
            super(mapper);
            this.converter = converter;
        }

        @NonNull
        @Override
        public Object read(@NonNull ResolvableType type, @NonNull HttpInputMessage inputMessage, Map<String, Object> hints) throws IOException, HttpMessageNotReadableException {
            Object payload = super.read(type, inputMessage, hints);
            if (converter.convertToRequestPayload(payload) instanceof Request request) {
                var requestContext = RequestContextHolder.getContext();
                if (requestContext == null) {
                    requestContext = new RequestContext(UUID.randomUUID().toString(), request.messageID());
                } else {
                    requestContext = new RequestContext(requestContext.conversationID(), request.messageID());
                }
                RequestContextHolder.setContext(requestContext);
            }
            return payload;
        }

        @Override
        public boolean canRead(@NonNull ResolvableType type, MediaType mediaType) {
            Class<?> raw = type.toClass();

            boolean isRequestPayload = Request.class.isAssignableFrom(raw);
            if (!isRequestPayload) {
                return super.canRead(type, mediaType);
            }

            if (mediaType == null
                    || mediaType.isWildcardType()
                    || MediaType.TEXT_PLAIN.includes(mediaType)
                    || MediaType.APPLICATION_OCTET_STREAM.includes(mediaType)) {
                return true;
            }

            return super.canRead(type, mediaType);
        }
    }
}
