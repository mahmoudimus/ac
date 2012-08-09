package com.atlassian.labs.remoteapps.util;

import com.atlassian.labs.remoteapps.modules.external.SchemaDocumented;
import com.atlassian.plugin.PluginParseException;
import org.apache.commons.io.IOUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import javax.servlet.ServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URI;
import java.net.URL;

import static com.atlassian.labs.remoteapps.api.XmlUtils.createSecureSaxReader;

/**
 *
 */
public class Dom4jUtils
{
    public static Element addSchemaDocumentation(Element source, SchemaDocumented generator)
    {
        Element doc = source.addElement("xs:annotation").addElement("xs:documentation");
        Element name = doc.addElement("name");
        if (generator.getName() != null)
        {
            name.setText(generator.getName());
        }
        Element desc = doc.addElement("description");
        if (generator.getDescription() != null)
        {
            desc.setText(generator.getDescription());
        }
        return doc;
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
            source = createSecureSaxReader().read(xmlUrl);
        }
        catch (DocumentException e)
        {
            throw new IllegalArgumentException("Unable to parse XML", e);
        }

        return source;
    }

    public static String printNode(Node document)
    {
        StringWriter writer = new StringWriter();
        XMLWriter xmlWriter = new XMLWriter(writer, OutputFormat.createPrettyPrint());
        try
        {
            xmlWriter.write(document);
        }
        catch (IOException e)
        {
            throw new IllegalArgumentException("Unable to write node", e);
        }
        return writer.toString();
    }

    public static Document readDocument(ServletRequest request)
    {
        try
        {
            return readDocument(request.getInputStream());
        }
        catch (IOException e)
        {
            // ignore
            return null;
        }
    }
    public static Document readDocument(InputStream in)
    {
        SAXReader build = createSecureSaxReader();
        try
        {
            return build.read(in);
        }
        catch (DocumentException e)
        {
            // don't care why
            return null;
        }
        finally
        {
            IOUtils.closeQuietly(in);
        }
    }

}
