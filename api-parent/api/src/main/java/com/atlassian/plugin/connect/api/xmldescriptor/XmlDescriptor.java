package com.atlassian.plugin.connect.api.xmldescriptor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Label code that is specific to the XML descriptors and should therefore be ported or retried with the
 * retirement of the XML descriptor. Allows such code to be more easily found, for example by XmlDescriptorAnnotationProcessor.
 */
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD, ElementType.CONSTRUCTOR})
public @interface XmlDescriptor
{
    String comment() default "";
}
