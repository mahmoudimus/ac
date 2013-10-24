package com.atlassian.plugin.connect.plugin.capabilities.descriptor.dom;

import com.atlassian.plugin.web.Condition;
import org.dom4j.Element;
import org.dom4j.dom.DOMElement;

import static org.apache.commons.lang.StringEscapeUtils.escapeHtml;

public class TabPanelElement
{
    private static final String KEY = "key";
    private static final String ORDER = "order";
    private static final String URL = "url";
    private static final String NAME = "name";
    private static final String LABEL = "label";
    private static final String CLASS = "class";
    private static final String CONDITION = "condition";
    private final Element domElement;

    public TabPanelElement(String domElementName)
    {
        this(new DOMElement(domElementName));
    }

    public TabPanelElement(Element element)
    {
        this.domElement = element;
    }

    public void setKey(String value)
    {
        domElement.addAttribute(KEY, value);
    }

    public String getKey()
    {
        return domElement.attributeValue(KEY);
    }

    public void setName(String value)
    {
        domElement.addAttribute(NAME, value);
    }

    public String getName()
    {
        return domElement.attributeValue(NAME);
    }

    public void setOrder(String order)
    {
        domElement.addElement(ORDER).setText(order);
    }

    public void setOrder(int order)
    {
        setOrder(Integer.toString(order));
    }

    public String getOrder()
    {
        return domElement.element(ORDER).getText();
    }

    public void setUrl(String value)
    {
        domElement.addAttribute(URL, value);
    }

    public String getUrl()
    {
        return domElement.attributeValue(URL);
    }

    public void setLabel(String name, String i18Key)
    {
        domElement.addElement(LABEL)
                .addAttribute(KEY, escapeHtml(i18Key))
                .setText(escapeHtml(name));
    }

    public void setModuleClass(Class<?> moduleClass)
    {
        domElement.addAttribute(CLASS, moduleClass.getName());
    }

    public void setCondition(Class<? extends Condition> conditionClass)
    {
        domElement.addElement(CONDITION).addAttribute(CLASS, conditionClass.getName());
    }

    public Element getElement()
    {
        return domElement;
    }
}
