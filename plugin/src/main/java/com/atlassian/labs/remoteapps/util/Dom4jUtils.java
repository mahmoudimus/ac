package com.atlassian.labs.remoteapps.util;

import com.atlassian.plugin.PluginParseException;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;

import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 */
public class Dom4jUtils
{
    private static final Set<String> STANDARD_ATTRIBUTES = ImmutableSet.of(
            "key",
            "name",
            "i18n-name-key"
    );

    public static Element copyDescriptorXml(Element source)
    {
        DocumentFactory factory = DocumentFactory.getInstance();
        Element copy = factory.createElement(source.getName());
        for (String name : STANDARD_ATTRIBUTES)
        {
            String value = source.attributeValue(name);
            if (value != null)
            {
                copy.addAttribute(name, value);
            }
        }

        Element sourceDesc = source.element("description");
        if (sourceDesc != null)
        {
            Element copyDesc = copy.addElement("description");
            String key = sourceDesc.attributeValue("key");
            if (key != null)
            {
                copyDesc.addAttribute("key", key);
            }
            copyDesc.setText(sourceDesc.getText());
        }
        return copy;
    }

    public static Element copyRequiredElements(Element source, Element dest, String... keys)
    {
        for (String name : keys)
        {
            HashSet<Element> elements = Sets.<Element>newHashSet(source.elements(name));
            if (elements.isEmpty())
            {
                throw new PluginParseException("Element '" + name + "' is required on '" + source.getName() + "'");
            }
            for (Element e : elements)
            {
                dest.add(e.createCopy());
            }
        }
        return dest;
    }

    public static Element copyOptionalElements(Element source, Element dest, String... keys)
    {
        for (String name : keys)
        {
            for (Element e : (List<Element>)source.elements(name))
            {
                dest.add(e.createCopy());
            }
        }
        return dest;
    }

    public static Element copyRequiredAttributes(Element source, Element dest, String... keys)
    {
        for (String name : keys)
        {
            dest.addAttribute(name, getRequiredAttribute(source, name));
        }
        return dest;
    }

    public static String getRequiredAttribute(Element e, String name)
    {
        String value = e.attributeValue(name);
        if (value == null)
        {
            throw new PluginParseException("Attribute '" + name + "' is required on '" + e.getName() + "'");
        }
        return value;
    }

    public static String getRequiredElementText(Element e, String name)
    {
        Element child = e.element(name);
        if (child == null)
        {
            throw new PluginParseException("Element '" + name + "' is required on '" + e.getName() + "'");
        }
        return child.getTextTrim();
    }

    public static URI getRequiredUriAttribute(Element e, String name)
    {
        String value = getRequiredAttribute(e, name);
        return URI.create(value);
    }

    public static URI getOptionalUriAttribute(Element e, String name)
    {
        String value = e.attributeValue(name);
        return value != null ? URI.create(value) : null;
    }

    public static String getOptionalAttribute(Element e, String name, Object defaultValue)
    {
        String value = e.attributeValue(name);
        return value != null ? value :
                defaultValue != null ? defaultValue.toString() : null;
    }



}
