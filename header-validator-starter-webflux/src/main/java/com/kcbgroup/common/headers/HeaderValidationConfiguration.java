package com.kcbgroup.common.headers;

import com.kcbgroup.common.headers.validators.DefaultHeaderValidator;
import com.kcbgroup.common.headers.validators.EpochTimestampValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;

@RequiredArgsConstructor
@AutoConfiguration
@EnableConfigurationProperties(HeaderValidationProperties.class)
@Import({HeaderValidationWebFilter.class, HeaderValidatorConverter.class, DefaultHeaderValidator.class, EpochTimestampValidator.class})
class HeaderValidationConfiguration {
}