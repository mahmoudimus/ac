package com.atlassian.plugin.connect.plugin.capabilities.descriptor.dom;

import com.atlassian.plugin.connect.plugin.capabilities.beans.AbstractConnectTabPanelCapabilityBean;
import com.atlassian.plugin.connect.spi.module.DynamicMarkerCondition;
import com.atlassian.plugin.web.Condition;
import com.google.common.base.Optional;
import org.dom4j.Element;
import org.dom4j.dom.DOMElement;


/**
 * A wrapper around the dom element for a tab panel descriptor so that only one place needs to know the structure of the dom.
 * As the compiler won't enforce whether we have used the correct names for attributes / elements or that we have accessed
 * an attribute as an element, the less places that do it the better.
 * A bit like a poor mans JAXB
 */
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

    public TabPanelElement(String domElementName, String modulePrefix, AbstractConnectTabPanelCapabilityBean bean,
                           Optional<? extends Class<?>> moduleClass)
    {
        this(domElementName);
        String completeKey = modulePrefix + bean.getKey();
        String name = bean.getName().getValue();

        setKey(completeKey);
        setName(name);
        setOrder(bean.getWeight());
        setUrl(bean.getUrl());
        setLabel(name, bean.getName().getI18n());
        setCondition(DynamicMarkerCondition.class);

        if (moduleClass.isPresent())
        {
            setModuleClass(moduleClass.get());
        }

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
        return domElement.elementText(ORDER);
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
                .addAttribute(KEY, i18Key)
                .setText(name);
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
