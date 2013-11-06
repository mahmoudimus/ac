package com.atlassian.plugin.connect.plugin.capabilities.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.atlassian.plugin.spring.scanner.ProductFilter;
import com.atlassian.plugin.connect.plugin.capabilities.provider.ConnectModuleProvider;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Marks a class as a Gson marshallable bean that represents a capability module.
 */
@Target(FIELD)
@Retention(RUNTIME)
@Documented
@Inherited
public @interface CapabilityModuleProvider
{
    Class<? extends ConnectModuleProvider> value();

    ProductFilter[] products() default {ProductFilter.ALL};
}
