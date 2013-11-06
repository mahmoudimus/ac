package com.atlassian.plugin.spring.scanner.annotation.component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.stereotype.Component;
/**
 * Annotation representing a component that should only be considered when running in JIRA.
 * Can be applied to Class type elements defined in the local project or to constructor params
 * where the param type is located in an external jar on this bundle's classpath
 */
@Target({ElementType.TYPE, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface JiraComponent
{
    String value() default "";
}
