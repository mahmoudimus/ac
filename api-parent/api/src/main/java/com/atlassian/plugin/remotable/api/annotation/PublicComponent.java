package com.atlassian.plugin.remotable.api.annotation;

import java.lang.annotation.ElementType;

@java.lang.annotation.Target(ElementType.TYPE)
@java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@java.lang.annotation.Documented
@javax.inject.Qualifier
public @interface PublicComponent
{
    /**
     * @return the interfaces to expose this component as
     */
    Class[] value();
}