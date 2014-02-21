package com.atlassian.plugin.connect.plugin.capabilities;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface ConvertToWiredTest
{
    /**
     * The optional reason why the test should be converted
     */
    String value() default "";
}
