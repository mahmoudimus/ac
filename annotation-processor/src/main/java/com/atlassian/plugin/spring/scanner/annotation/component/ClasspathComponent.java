package com.atlassian.plugin.spring.scanner.annotation.component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.stereotype.Component;

/**
 * Annotation for constructor args representing components that need to live in the local bean container
 * but are located in external jars on this bundle's classpath.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface ClasspathComponent
{
    String value() default "";
}
