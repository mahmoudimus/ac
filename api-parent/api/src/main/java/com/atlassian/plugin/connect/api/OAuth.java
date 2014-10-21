package com.atlassian.plugin.connect.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Label code that is specific to OAuth authentication and should therefore be ported or retired with support for this authentication method.
 * Allows such code to be more easily found.
 */
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD, ElementType.CONSTRUCTOR, ElementType.LOCAL_VARIABLE})
public @interface OAuth
{
}
