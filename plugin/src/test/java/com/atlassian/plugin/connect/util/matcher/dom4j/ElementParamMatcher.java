package com.atlassian.plugin.connect.util.matcher.dom4j;

import com.google.common.base.Objects;
import org.dom4j.Element;
import org.hamcrest.Description;
import org.mockito.ArgumentMatcher;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public abstract class ElementParamMatcher extends ArgumentMatcher<Element>
{
    private final String name;
    private final String expectedValue;

    public ElementParamMatcher(String name, String expectedValue)
    {
        this.name = checkNotNull(name);
        this.expectedValue = checkNotNull(expectedValue);
    }

    @Override
    public boolean matches(Object argument)
    {
        assertThat(argument, is(instanceOf(Element.class)));
        Element element = (Element) argument;
        return matchesOnElement(element, name, expectedValue);
    }

    protected boolean matchesOnElement(Element element, String name, String expectedValue)
    {
        return Objects.equal(getValue(element, name), expectedValue);
    }

    protected abstract String getValue(Element element, String name);

    @Override
    public void describeTo(Description description)
    {
        description.appendText("Element with param ");
        description.appendValue(name);
        description.appendText(" = ");
        description.appendValue(expectedValue);
    }
}
