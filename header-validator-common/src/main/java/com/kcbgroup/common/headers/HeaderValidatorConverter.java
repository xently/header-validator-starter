package com.kcbgroup.common.headers;

import com.kcbgroup.common.headers.validators.DefaultHeaderValidator;
import com.kcbgroup.common.headers.validators.HeaderValidator;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.boot.context.properties.ConfigurationPropertiesBinding;
import org.springframework.context.ApplicationContext;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.util.function.Function;

@Slf4j
@Component
@ConfigurationPropertiesBinding
@AllArgsConstructor
class HeaderValidatorConverter implements Converter<String, HeaderValidator> {
    private final ApplicationContext context;

    @Override
    public HeaderValidator convert(@NonNull String source) {
        var headerValidatorSource = context.getEnvironment().getProperty(
                "kcb.api.headers.validator.source",
                HeaderValidatorSource.class,
                HeaderValidatorSource.FQCNB4BeanDefinition
        );

        return switch (headerValidatorSource) {
            case FQCN -> getHeaderValidatorFromFQCN(source);
            case BeanDefinition -> getHeaderValidatorFromBeanDefinition(source);
            case FQCNB4BeanDefinition ->
                    getHeaderValidatorFromFQCN(source, e -> getHeaderValidatorFromBeanDefinition(source));
            case BeanDefinitionB4FQCN ->
                    getHeaderValidatorFromBeanDefinition(source, e -> getHeaderValidatorFromFQCN(source));
        };
    }

    private HeaderValidator getHeaderValidatorFromBeanDefinition(String source) {
        return getHeaderValidatorFromBeanDefinition(source, e -> {
            log.error("Bean definition retrieval failed for instance of type '{}' from '{}'.", HeaderValidator.class.getName(), source, e);
            return new DefaultHeaderValidator();
        });
    }

    private HeaderValidator getHeaderValidatorFromBeanDefinition(String source, Function<Exception, HeaderValidator> fallback) {
        try {
            return context.getBean(source, HeaderValidator.class);
        } catch (BeansException e) {
            return fallback.apply(e);
        }
    }

    private static HeaderValidator getHeaderValidatorFromFQCN(String source) {
        return getHeaderValidatorFromFQCN(source, e -> {
            log.error("Failed to create an instance of '{}' from '{}'.", HeaderValidator.class.getName(), source, e);
            return new DefaultHeaderValidator();
        });
    }

    private static HeaderValidator getHeaderValidatorFromFQCN(String source, Function<Exception, HeaderValidator> fallback) {
        try {
            return (HeaderValidator) Class.forName(source)
                    .getDeclaredConstructor()
                    .newInstance();
        } catch (ClassNotFoundException
                 | InvocationTargetException
                 | InstantiationException
                 | IllegalAccessException
                 | NoSuchMethodException e) {
            return fallback.apply(e);
        }
    }
}
