package com.atlassian.plugin.connect.plugin.capabilities.annotation;

import com.atlassian.plugin.connect.plugin.capabilities.provider.ConnectModuleProvider;
import com.atlassian.plugin.spring.scanner.ProductFilter;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Marks a class as a Gson marshallable bean that represents a connect module.
 */
@Target(FIELD)
@Retention(RUNTIME)
@Documented
@Inherited
public @interface ConnectModule
{
    Class<? extends ConnectModuleProvider> value();

    ProductFilter[] products() default {ProductFilter.ALL};
}
