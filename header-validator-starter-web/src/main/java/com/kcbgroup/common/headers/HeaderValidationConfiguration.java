package com.kcbgroup.common.headers;

import com.kcbgroup.common.headers.validators.DefaultHeaderValidator;
import com.kcbgroup.common.headers.validators.EpochTimestampValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@RequiredArgsConstructor
@AutoConfiguration
@EnableConfigurationProperties(HeaderValidationProperties.class)
@Import({HeaderValidatorConverter.class, DefaultHeaderValidator.class, EpochTimestampValidator.class})
class HeaderValidationConfiguration implements WebMvcConfigurer {
    private final HeaderValidationProperties properties;

    @Override
    public void addInterceptors(@NonNull InterceptorRegistry registry) {
        registry.addInterceptor(new HeaderValidationInterceptor(properties));
    }
}