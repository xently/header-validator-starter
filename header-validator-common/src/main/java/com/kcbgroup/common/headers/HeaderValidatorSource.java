package com.kcbgroup.common.headers;

public enum HeaderValidatorSource {
    /**
     * Fully Qualified Class Name.
     * <p>
     * This requires the validator class to be public and should not have a constructor that takes parameters.
     */
    FQCN,
    /**
     * Bean Definition.
     * <p>
     * This requires the validator to be registered as a bean in the Spring Application Context.
     */
    BeanDefinition,
    /**
     * Lookup by Fully Qualified Class Name before falling back to Bean Definition.
     */
    FQCNB4BeanDefinition,
    /**
     * Lookup by Bean Definition before falling back to Fully Qualified Class Name.
     */
    BeanDefinitionB4FQCN
}
