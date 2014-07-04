package it.com.atlassian.plugin.connect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicate that this class is the basis for a parameterized test to be run by AtlassianPluginsTestRunner.
 * At test compilation time a subclass in the same package with each {@link Parameters} row enumerated as a org.junit.Test-annotated method will be constructed.
 * Test, Before, BeforeClass, After & AfterClass annotations on methods in superclasses will be inherited.
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
public @interface ParameterizedWiredTest
{
    /**
     * Indicate an Object[][] field that is the source of parameters for {@link Test}-annotated methods.
     * There must be exactly one per {@link ParameterizedWiredTest}-annotated class.
     * This field must be readable from subclasses.
     */
    @Retention(RetentionPolicy.CLASS)
    @Target(ElementType.FIELD)
    public @interface Parameters
    {
        String name() default "";
        Class[] classes() default {};
        int length() default -1; // because the length of the array appears not to be available to compile-time annotation processors
    }

    /**
     * Indicate that a method is a test to be run with the parameters from the {@link Parameters}-annotated field.
     * We can't use junit @Test annotation because wired tests insist that junit.Test methods have no parameters.
     * There must be at least one per {@link ParameterizedWiredTest}-annotated class and there may be many.
     * These methods must be callable from subclasses.
     */
    @Retention(RetentionPolicy.CLASS)
    @Target(ElementType.METHOD)
    public @interface Test
    {}
}
