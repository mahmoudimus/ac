package com.atlassian.plugin.remotable.api.annotation;

import com.google.inject.BindingAnnotation;

import java.lang.annotation.ElementType;

@java.lang.annotation.Target({ElementType.PARAMETER, ElementType.FIELD})
@java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@java.lang.annotation.Documented
@BindingAnnotation
public @interface ComponentImport
{
}