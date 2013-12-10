package com.atlassian.json.schema.annotation;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target(FIELD)
@Retention(RUNTIME)
@Inherited
public @interface NumericSchemaAttributes
{
    double multipleOf() default -1;
    
    double maximum() default Double.MAX_VALUE;
    boolean exclusiveMaximum() default false;
    
    double minimum() default Double.MIN_VALUE;
    boolean exclusiveMinimum() default false;
    
    
}
