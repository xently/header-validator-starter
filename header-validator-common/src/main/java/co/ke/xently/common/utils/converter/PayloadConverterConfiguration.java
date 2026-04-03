package co.ke.xently.common.utils.converter;

import co.ke.xently.common.utils.HeaderValidationErrorResponseHandler;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import(HeaderValidationErrorResponseHandler.class)
class PayloadConverterConfiguration {
}
