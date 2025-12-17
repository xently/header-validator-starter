package com.kcbgroup.common.utils.converter;

import com.kcbgroup.common.utils.HeaderValidationErrorResponseHandler;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import(HeaderValidationErrorResponseHandler.class)
class PayloadConverterConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public PayloadConverter payloadConverter() {
        return new PayloadConverter() {
        };
    }
}
