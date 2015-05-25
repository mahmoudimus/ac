/*
 BSD License

 Copyright (c) 2000-2006, www.hamcrest.org
 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:

 Redistributions of source code must retain the above copyright notice, this list of
 conditions and the following disclaimer. Redistributions in binary form must reproduce
 the above copyright notice, this list of conditions and the following disclaimer in
 the documentation and/or other materials provided with the distribution.

 Neither the name of Hamcrest nor the names of its contributors may be used to endorse
 or promote products derived from this software without specific prior written
 permission.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
 SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
 WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH
 DAMAGE.
 */

package com.atlassian.plugin.connect.confluence.capabilities.bean.matchers;

import org.hamcrest.Description;
import org.hamcrest.DiagnosingMatcher;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.hamcrest.collection.IsIterableContainingInAnyOrder;
import org.hamcrest.core.AllOf;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.atlassian.plugin.connect.confluence.capabilities.bean.matchers.IsEmptyMap.emptyMap;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.beans.PropertyUtil.NO_ARGUMENTS;
import static org.hamcrest.beans.PropertyUtil.propertyDescriptorsFor;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.hamcrest.core.IsEqual.equalTo;

/**
 * This class is based on Hamcrest's {@link org.hamcrest.beans.SamePropertyValuesAs} matcher,
 * but does a deep comparison, instead of a shallow one.
 *
 * It accounts for nested beans, collections of beans and maps.
 */
public class SameDeepPropertyValuesAs<T> extends TypeSafeDiagnosingMatcher<T>
{
    private final T expectedBean;
    private final Set<String> propertyNames;
    private final List<PropertyMatcher> propertyMatchers;


    public SameDeepPropertyValuesAs(T expectedBean)
    {
        PropertyDescriptor[] descriptors = propertyDescriptorsFor(expectedBean, Object.class);
        this.expectedBean = expectedBean;
        this.propertyNames = propertyNamesFrom(descriptors);
        this.propertyMatchers = propertyMatchersFor(expectedBean, descriptors);
    }

    @Override
    public boolean matchesSafely(T bean, Description mismatch)
    {
        return isCompatibleType(bean, mismatch)
                && hasNoExtraProperties(bean, mismatch)
                && hasMatchingValues(bean, mismatch);
    }

    @Override
    public void describeTo(Description description)
    {
        description.appendText("same property values as " + expectedBean.getClass().getSimpleName())
                .appendList(" [", ", ", "]", propertyMatchers);
    }


    private boolean isCompatibleType(T item, Description mismatchDescription)
    {
        if (!expectedBean.getClass().isAssignableFrom(item.getClass()))
        {
            mismatchDescription.appendText("is incompatible type: " + item.getClass().getSimpleName());
            return false;
        }
        return true;
    }

    private boolean hasNoExtraProperties(T item, Description mismatchDescription)
    {
        Set<String> actualPropertyNames = propertyNamesFrom(propertyDescriptorsFor(item, Object.class));
        actualPropertyNames.removeAll(propertyNames);
        if (!actualPropertyNames.isEmpty())
        {
            mismatchDescription.appendText("has extra properties called " + actualPropertyNames);
            return false;
        }
        return true;
    }

    private boolean hasMatchingValues(T item, Description mismatchDescription)
    {
        for (PropertyMatcher propertyMatcher : propertyMatchers)
        {
            if (!propertyMatcher.matches(item))
            {
                propertyMatcher.describeMismatch(item, mismatchDescription);
                return false;
            }
        }
        return true;
    }

    private static <T> List<PropertyMatcher> propertyMatchersFor(T bean, PropertyDescriptor[] descriptors)
    {
        List<PropertyMatcher> result = new ArrayList<PropertyMatcher>(descriptors.length);
        for (PropertyDescriptor propertyDescriptor : descriptors)
        {
            result.add(new PropertyMatcher(propertyDescriptor, bean));
        }
        return result;
    }

    private static Set<String> propertyNamesFrom(PropertyDescriptor[] descriptors)
    {
        HashSet<String> result = new HashSet<String>();
        for (PropertyDescriptor propertyDescriptor : descriptors)
        {
            result.add(propertyDescriptor.getDisplayName());
        }
        return result;
    }

    public static class PropertyMatcher extends DiagnosingMatcher<Object>
    {
        private final Method readMethod;
        private Matcher matcher;
        private final String propertyName;

        public PropertyMatcher(PropertyDescriptor descriptor, Object expectedObject)
        {
            this.propertyName = descriptor.getDisplayName();
            this.readMethod = descriptor.getReadMethod();
            this.matcher = createMatcher(readProperty(readMethod, expectedObject));
        }

        private static Matcher createMatcher(Object object)
        {
            if (null == object)
            {
                return equalTo(object);
            }
            if (Iterable.class.isAssignableFrom(object.getClass()))
            {
                Iterator<Object> iterator = ((Iterable<Object>) object).iterator();
                if (!iterator.hasNext())
                {
                    return empty();
                }
                List<Matcher> matchers = createCollectionMatchers(iterator);
                return new IsIterableContainingInAnyOrder(matchers);
            }
            if (Map.class.isAssignableFrom(object.getClass()))
            {
                Map<Object, Object> map = (Map<Object, Object>) object;
                if (map.isEmpty())
                {
                    return emptyMap();
                }
                List<Matcher> matchers = createMapMatchers(map);
                return new AllOf(matchers);
            }
            if (Comparable.class.isAssignableFrom(object.getClass()))
            {
                return equalTo(object);
            }
            return sameDeepPropertyValuesAs(object);
        }

        private static List<Matcher> createCollectionMatchers(Iterator<Object> iterator)
        {
            List<Matcher> matchers = new ArrayList<Matcher>();
            while (iterator.hasNext()) {
                Object object = iterator.next();
                Matcher itemMatcher = createMatcher(object);
                matchers.add(itemMatcher);
            }
            return matchers;
        }

        private static List<Matcher> createMapMatchers(Map<Object, Object> map)
        {
            List<Matcher> matchers = new ArrayList<Matcher>(map.size());
            for (Map.Entry<Object, Object> entry : map.entrySet())
            {
                Matcher keyMatcher = createMatcher(entry.getKey());
                Matcher valueMatcher = createMatcher(entry.getValue());
                Matcher entryMatcher = hasEntry(keyMatcher, valueMatcher);
                matchers.add(entryMatcher);
            }
            return matchers;
        }

        @Override
        public boolean matches(Object actual, Description mismatch)
        {
            final Object actualValue = readProperty(readMethod, actual);
            if (!matcher.matches(actualValue))
            {
                mismatch.appendText(propertyName + " ");
                matcher.describeMismatch(actualValue, mismatch);
                return false;
            }
            return true;
        }

        @Override
        public void describeTo(Description description)
        {
            description.appendText(propertyName + ": ").appendDescriptionOf(matcher);
        }
    }

    private static Object readProperty(Method method, Object target)
    {
        try
        {
            return method.invoke(target, NO_ARGUMENTS);
        }
        catch (Exception e)
        {
            throw new IllegalArgumentException("Could not invoke " + method + " on " + target, e);
        }
    }

    /**
     * Creates a matcher that matches when the examined object has values for all of
     * its JavaBean properties that are equal to the corresponding values of the
     * specified bean.
     * <p/>
     * For example:
     * <pre>assertThat(myBean, samePropertyValuesAs(myExpectedBean))</pre>
     *
     * @param expectedBean the bean against which examined beans are compared
     */
    @Factory
    public static <T> Matcher<T> sameDeepPropertyValuesAs(T expectedBean)
    {
        return new SameDeepPropertyValuesAs<T>(expectedBean);
    }

}
