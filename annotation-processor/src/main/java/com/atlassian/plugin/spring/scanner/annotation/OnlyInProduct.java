package com.atlassian.plugin.spring.scanner.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.atlassian.plugin.spring.scanner.ProductFilter;

/**
 * Used to annotate product specific annotations to designate which product they belong to
 */
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface OnlyInProduct
{
    ProductFilter value();
}
