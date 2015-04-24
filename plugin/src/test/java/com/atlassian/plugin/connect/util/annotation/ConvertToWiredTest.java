package com.atlassian.plugin.connect.util.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface ConvertToWiredTest
{
    /**
     * @return the optional reason why the test should be converted
     */
    String value() default "";
}
