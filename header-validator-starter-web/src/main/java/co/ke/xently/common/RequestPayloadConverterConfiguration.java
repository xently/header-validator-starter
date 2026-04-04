package co.ke.xently.common;

import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@AutoConfiguration
@AllArgsConstructor
@Import({HeaderValidatorExceptionHandler.class})
class RequestPayloadConverterConfiguration implements WebMvcConfigurer {
}
