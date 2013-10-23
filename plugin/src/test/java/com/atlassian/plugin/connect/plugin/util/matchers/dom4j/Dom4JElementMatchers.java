package com.atlassian.plugin.connect.plugin.util.matchers.dom4j;

import org.dom4j.Element;
import org.mockito.ArgumentMatcher;

public class Dom4JElementMatchers
{
    public static ArgumentMatcher<Element> hasAttributeValue(String name, String expectedValue)
    {
        return new ElementAttributeParamMatcher(name, expectedValue);
    }

    public static ArgumentMatcher<Element> hasSubElementAttributeValue(String subElementName, String attributeName, String expectedValue)
    {
        return new SubElementParamMatcher(subElementName, new ElementAttributeParamMatcher(attributeName, expectedValue));
    }


    public static ArgumentMatcher<Element> hasSubElementTextValue(String name, String expectedValue)
    {
        return new ElementSubElementTextMatcher(name, expectedValue);
    }

}
