package com.atlassian.labs.remoteapps.api.annotation;

import java.lang.annotation.ElementType;

@java.lang.annotation.Target({ElementType.PARAMETER, ElementType.FIELD})
@java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@java.lang.annotation.Documented
public @interface ServiceReference {

    java.lang.String value() default "";
}