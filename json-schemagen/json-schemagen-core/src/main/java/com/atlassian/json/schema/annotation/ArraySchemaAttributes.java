package com.atlassian.json.schema.annotation;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target(FIELD)
@Retention(RUNTIME)
@Inherited
public @interface ArraySchemaAttributes
{
    boolean additionalItems() default false;
    int maxItems() default Integer.MAX_VALUE;
    int minItems() default Integer.MIN_VALUE;
    boolean uniqueItems() default false;
    
}
