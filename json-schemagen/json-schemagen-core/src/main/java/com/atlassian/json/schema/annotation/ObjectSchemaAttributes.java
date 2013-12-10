package com.atlassian.json.schema.annotation;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target(FIELD)
@Retention(RUNTIME)
@Inherited
public @interface ObjectSchemaAttributes
{
    /* Common Attributes */
    String[] allowedValues() default {AnnotationHelper.EMPTY_ENUM}; //gets turned into "enum"
    String[] type() default {AnnotationHelper.EMPTY_TYPE};
    Class[] allOf() default {AnnotationHelper.EmptyClass.class};
    Class[] anyOf() default {AnnotationHelper.EmptyClass.class};
    Class[] oneOf() default {AnnotationHelper.EmptyClass.class};
    Class not() default AnnotationHelper.EmptyClass.class;
    Class definitions() default AnnotationHelper.EmptyClass.class;
    String defaultValue() default "";
    String title() default "";
    String description() default "";
    
    /* Object Attributes */
    int maxProperties() default Integer.MAX_VALUE;
    int minProperties() default Integer.MIN_VALUE;
    String[] required() default {""};
    boolean additionalProperties() default false;
    String[] patternProperties() default {""};
    SchemaDependency[] dependencies() default {};
}
