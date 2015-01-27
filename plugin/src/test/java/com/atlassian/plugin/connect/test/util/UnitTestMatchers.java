package com.atlassian.plugin.connect.test.util;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.predicate.ModuleDescriptorPredicate;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

/**
 * Matchers useful for writing unit tests
 */
public final class UnitTestMatchers
{
    /**
     * Returns a matcher that will accept {@link com.atlassian.plugin.predicate.ModuleDescriptorPredicate}s which
     * return true for a given example Module descriptor.
     *
     * <p>
     *     Useful when we want to make sure the class we are testing asks for the right modules and want to
     *     assume certain modules are returned.
     * </p>
     *
     * @param exampleDescriptor descriptor that must be matched by the predicate
     * @param <T> Type of class the module holds
     * @return matcher over ModuleDescriptorPredicate
     */
    public static <T> Matcher<ModuleDescriptorPredicate<T>> predicateThatWillMatch(final ModuleDescriptor<T> exampleDescriptor)
    {
        return new TypeSafeMatcher<ModuleDescriptorPredicate<T>>()
        {
            @Override
            protected boolean matchesSafely(final ModuleDescriptorPredicate<T> item)
            {
                return item.matches(exampleDescriptor);
            }

            @Override
            public void describeTo(final Description description)
            {
                description.appendText("predicate that matches a " + exampleDescriptor.getClass().toString());
            }
        };
    }
}
