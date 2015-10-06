package com.atlassian.plugin.connect.spi.util;

import org.apache.commons.io.IOUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import javax.servlet.ServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import static com.atlassian.plugin.connect.spi.util.XmlUtils.createSecureSaxReader;

public final class Dom4jUtils
{

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
