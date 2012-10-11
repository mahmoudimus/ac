package com.atlassian.plugin.remotable.junit;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
@Documented
/**
 * <p>This is the annotation to tell your test which apps should be loaded in the container.</p>
 * <p>Note that for maven modules, the paths support the <strong>${moduleDir}</strong> place holder, allowing for simple
 * paths reference to source code within the same module.</p>
 */
public @interface UniversalBinaries
{
    String[] value();

    Mode mode() default Mode.PROPERTY;
}
