package com.atlassian.plugin.connect.api.capabilities.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.atlassian.plugin.connect.api.capabilities.provider.ConnectModuleProvider;

import org.atteo.evo.classindex.IndexAnnotated;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Marks a class as a Gson marshallable bean that represents a capability module.
 */
@Target(TYPE)
@Retention(RUNTIME)
@Documented
@IndexAnnotated
@Inherited
public @interface CapabilitySet
{
    /**
     * The key of the capability in the root capabilities json
     * @return
     */
    String key();
    
    Class<? extends ConnectModuleProvider> moduleProvider();

}
