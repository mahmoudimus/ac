package com.atlassian.plugin.connect.util.matcher.dom4j;

import org.dom4j.Element;
import org.hamcrest.Description;
import org.mockito.ArgumentMatcher;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class SubElementParamMatcher extends ArgumentMatcher<Element>
{
    private final String subElementName;
    private final ElementParamMatcher paramMatcher;

    public SubElementParamMatcher(String subElementName, ElementParamMatcher paramMatcher)
    {
        this.subElementName = subElementName;
        this.paramMatcher = paramMatcher;
    }

    @Override
    public boolean matches(Object argument)
    {
        assertThat(argument, is(instanceOf(Element.class)));
        Element element = (Element) argument;
        return paramMatcher.matches(element.element(subElementName));
    }

    @Override
    public void describeTo(Description description)
    {
        paramMatcher.describeTo(description);
    }
}
