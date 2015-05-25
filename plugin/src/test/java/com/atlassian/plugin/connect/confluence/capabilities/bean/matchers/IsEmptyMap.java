package com.atlassian.plugin.connect.confluence.capabilities.bean.matchers;

import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import java.util.Map;

public class IsEmptyMap<K, V> extends TypeSafeMatcher<Map<? extends K, ? extends V>>
{
    @Override
    protected boolean matchesSafely(Map<? extends K, ? extends V> map)
    {
        return map.isEmpty();
    }

    @Override
    protected void describeMismatchSafely(Map<? extends K, ? extends V> map, Description mismatchDescription)
    {
        mismatchDescription.appendText("map was ").appendValueList("[", ", ", "]", map.entrySet());
    }

    @Override
    public void describeTo(Description description)
    {
        description.appendText("an empty map");
    }

    @Factory
    public static <K, V> Matcher<Map<? extends K, ? extends V>> emptyMap()
    {
        return new IsEmptyMap<K, V>();
    }
}
