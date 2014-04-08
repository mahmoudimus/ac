package com.atlassian.json.schema.annotation;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Used to designate that the annotated field/type should be ignored when generating the schema
 * 
 * NOTE: currently only field level is ignored
 */
@Target({FIELD,TYPE})
@Retention(RUNTIME)
@Inherited
public @interface SchemaIgnore
{
    /**
     * A string which we can use to filter ignores.
     * If this is blank, the field will be ignored.
     * If this is set and the generation was run with an "ignoreFilter" matching this string, it will be ignored.
     * If this is set but does not match the "ignoreFilter" it will not be ignored.
     * @return
     */
    String value() default "";
}
