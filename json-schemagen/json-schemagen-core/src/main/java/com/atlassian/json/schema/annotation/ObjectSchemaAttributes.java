package com.atlassian.json.schema.annotation;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target(TYPE)
@Retention(RUNTIME)
@Inherited
public @interface ObjectSchemaAttributes
{
    int maxProperties() default Integer.MAX_VALUE;
    int minProperties() default Integer.MIN_VALUE;
    boolean additionalProperties() default false;
    String[] patternProperties() default {};
    SchemaDependency[] dependencies() default {};
    FieldDocOverride[] docOverrides() default {};
}
