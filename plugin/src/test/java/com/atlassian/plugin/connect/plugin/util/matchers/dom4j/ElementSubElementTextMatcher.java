package com.atlassian.plugin.connect.plugin.util.matchers.dom4j;

import org.dom4j.Element;

public class ElementSubElementTextMatcher extends ElementParamMatcher
{
    public ElementSubElementTextMatcher(String name, String expectedValue)
    {
        super(name, expectedValue);
    }

    @Override
    protected String getValue(Element element, String name)
    {
        return element.element(name).getText();
    }
}
