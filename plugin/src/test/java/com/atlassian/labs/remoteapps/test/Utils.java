package com.atlassian.labs.remoteapps.test;

import com.atlassian.pageobjects.ProductInstance;
import org.apache.commons.io.IOUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.MalformedURLException;
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


}
