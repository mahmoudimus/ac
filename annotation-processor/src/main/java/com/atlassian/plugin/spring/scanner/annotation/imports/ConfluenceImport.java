package com.atlassian.plugin.spring.scanner.annotation.imports;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.atlassian.plugin.spring.scanner.ProductFilter;
import com.atlassian.plugin.spring.scanner.annotation.OnlyInProduct;

/**
 * Annotation representing an OSGi service that's required to be imported into this bundle
 * when running within Confluence. Can be applied to constructor params where the param type 
 * is a service interface exported by another bundle
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@ComponentImport
@OnlyInProduct(ProductFilter.CONFLUENCE)
public @interface ConfluenceImport
{
    String value() default "";
}
