package com.atlassian.plugin.connect.plugin.capabilities.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.atlassian.plugin.connect.plugin.capabilities.provider.ConnectModuleProvider;
import com.atlassian.plugin.connect.plugin.capabilities.provider.NullModuleProvider;

import org.atteo.evo.classindex.IndexAnnotated;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Marks a class as a Gson marshallable bean that represents a capability module.
 */
@Target(FIELD)
@Retention(RUNTIME)
@Documented
@IndexAnnotated
@Inherited
public @interface CapabilityModuleProvider
{
    Class<? extends ConnectModuleProvider> value();
    ProductFilter[] products() default {ProductFilter.ALL};
}
