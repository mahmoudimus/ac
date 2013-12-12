package com.atlassian.json.schema.annotation;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target(FIELD)
@Retention(RUNTIME)
@Inherited
public @interface CommonSchemaAttributes
{
    Class[] allOf() default {};
    Class[] anyOf() default {};
    Class[] oneOf() default {};
    Class not() default AnnotationHelper.EmptyClass.class;
    SchemaDefinition[] definitions() default {};
    String defaultValue() default "";
    String title() default "";
    String description() default "";
}
