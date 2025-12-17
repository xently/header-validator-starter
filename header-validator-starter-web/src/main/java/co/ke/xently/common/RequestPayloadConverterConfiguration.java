package co.ke.xently.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import co.ke.xently.common.utils.converter.PayloadConverter;
import co.ke.xently.common.utils.dto.Request;
import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.UUID;

@AutoConfiguration
@AllArgsConstructor
@ConditionalOnClass(ObjectMapper.class)
@AutoConfigureAfter(JacksonAutoConfiguration.class)
@Import({RequestContextFilter.class, HeaderValidatorExceptionHandler.class})
class RequestPayloadConverterConfiguration implements WebMvcConfigurer {
    private final ObjectMapper objectMapper;
    private final PayloadConverter payloadConverter;

    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.addFirst(new RequestPayloadJacksonConverter(objectMapper, payloadConverter));
    }

    static class RequestPayloadJacksonConverter extends MappingJackson2HttpMessageConverter {
        private final PayloadConverter converter;

        RequestPayloadJacksonConverter(ObjectMapper mapper, PayloadConverter converter) {
            super(mapper);
            this.converter = converter;
        }

        @NonNull
        @Override
        public Object read(@NonNull Type type, Class<?> contextClass, @NonNull HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
            Object payload = super.read(type, contextClass, inputMessage);
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
        public boolean canRead(@NonNull Type type, Class<?> contextClass, MediaType mediaType) {
            if (type instanceof ParameterizedType parameterizedType) {
                return canRead(parameterizedType.getRawType(), contextClass, mediaType);
            }

            Class<?> raw;
            try {
                raw = (Class<?>) type;
            } catch (ClassCastException e) {
                raw = null;
            }

            boolean isRequestPayload = raw != null && Request.class.isAssignableFrom(raw);
            if (!isRequestPayload) {
                return super.canRead(type, contextClass, mediaType);
            }

            if (mediaType == null
                    || mediaType.isWildcardType()
                    || MediaType.TEXT_PLAIN.includes(mediaType)
                    || MediaType.APPLICATION_OCTET_STREAM.includes(mediaType)) {
                return true;
            }

            return super.canRead(type, contextClass, mediaType);
        }
    }
}
