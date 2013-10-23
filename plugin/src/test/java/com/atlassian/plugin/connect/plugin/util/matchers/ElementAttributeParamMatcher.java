package com.atlassian.plugin.connect.plugin.util.matchers;

import org.dom4j.Element;

public class ElementAttributeParamMatcher extends ElementParamMatcher
{
    public ElementAttributeParamMatcher(String name, String expectedValue)
    {
        super(name, expectedValue);
    }

    @Override
    protected String getValue(Element element, String name)
    {
        return element.attributeValue(name);
    }

}
