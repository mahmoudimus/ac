package com.atlassian.labs.remoteapps.util;

import com.atlassian.labs.remoteapps.descriptor.external.RemoteModuleDescriptor;
import com.atlassian.labs.remoteapps.installer.InstallationFailedException;
import com.atlassian.plugin.PluginParseException;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import org.apache.commons.io.IOUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.xml.sax.InputSource;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.net.URI;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 */
public class Dom4jUtils
{
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

    public static Element copyOptionalAttributes(Element source, Element dest, String... keys)
    {
        for (String name : keys)
        {
            String value = getOptionalAttribute(source, name, null);
            if (value != null)
            {
                dest.addAttribute(name, value);
            }
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

    public static Document parseDocument(URL xmlUrl)
    {
        Document source;
        try
        {
            source = new SAXReader().read(xmlUrl);
        }
        catch (DocumentException e)
        {
            throw new IllegalArgumentException("Unable to parse XML", e);
        }

        return source;
    }

    public static String printDocument(Document document)
    {
        StringWriter writer = new StringWriter();
        XMLWriter xmlWriter = new XMLWriter(writer, OutputFormat.createPrettyPrint());
        try
        {
            xmlWriter.write(document);
        }
        catch (IOException e)
        {
            throw new IllegalArgumentException("Unable to write document", e);
        }
        return writer.toString();
    }

}
