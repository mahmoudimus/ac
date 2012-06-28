package com.atlassian.labs.remoteapps.api;

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
import java.net.ServerSocket;
import java.net.URL;

import static com.atlassian.labs.remoteapps.api.XmlUtils.createSecureSaxReader;

/**
 *
 */
public class Utils
{
    public static String loadResourceAsString(String path) throws IOException
    {
        return IOUtils.toString(Utils.class.getClassLoader().getResourceAsStream(path));
    }

    public static String loadResourceAsString(Class caller, String path) throws IOException
    {
        return IOUtils.toString(caller.getResourceAsStream(path));
    }

    public static String getJson(String url) throws IOException
    {
        InputStream in = new URL(url).openStream();
        StringWriter writer = new StringWriter();
        IOUtils.copy(in, writer);
        in.close();
        return writer.toString();
    }

    public static Document getXml(String url) throws IOException, DocumentException
    {
        InputStream inputStream = new URL(url).openStream();
        return createSecureSaxReader().read(inputStream);
    }

    public static void emptyGet(String url) throws IOException
    {
        InputStream in = new URL(url).openStream();
        in.close();
    }

    public static int pickFreePort()
    {
        ServerSocket socket = null;
        try
        {
            socket = new ServerSocket(0);
            return socket.getLocalPort();
        } catch (IOException e)
        {
            throw new RuntimeException("Error opening socket", e);
        } finally
        {
            if (socket != null)
            {
                try
                {
                    socket.close();
                } catch (IOException e)
                {
                    throw new RuntimeException("Error closing socket", e);
                }
            }
        }
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
