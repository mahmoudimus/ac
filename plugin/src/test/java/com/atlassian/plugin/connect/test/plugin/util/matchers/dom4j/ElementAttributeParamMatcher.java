package com.atlassian.plugin.connect.test.plugin.util.matchers.dom4j;

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
