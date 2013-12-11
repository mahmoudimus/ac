package com.atlassian.json.schema.annotation;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target(FIELD)
@Retention(RUNTIME)
@Inherited
public @interface StringSchemaAttributes
{
    String pattern() default "";
    int maxLength() default Integer.MAX_VALUE;
    int minLength() default Integer.MIN_VALUE;
    
    //for now this is only a string attr but in theory it could be a common attr
    String format() default "";
    
}
