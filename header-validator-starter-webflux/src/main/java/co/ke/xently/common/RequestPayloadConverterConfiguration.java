package co.ke.xently.common;

import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.web.reactive.config.WebFluxConfigurer;

@AutoConfiguration
@AllArgsConstructor
@Import({HeaderValidatorExceptionHandler.class})
class RequestPayloadConverterConfiguration implements WebFluxConfigurer {
}
