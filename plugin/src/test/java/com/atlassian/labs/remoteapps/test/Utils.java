package com.atlassian.labs.remoteapps.test;

import com.atlassian.labs.remoteapps.container.internal.Environment;
import org.apache.commons.io.IOUtils;
import org.bouncycastle.openssl.PEMWriter;
import org.dom4j.Document;
import org.dom4j.DocumentException;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.ServerSocket;
import java.net.URL;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

import static com.atlassian.labs.remoteapps.spi.util.XmlUtils.createSecureSaxReader;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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

    public static RunnerSignedRequestHandler createSignedRequestHandler(String appKey) throws NoSuchAlgorithmException,
            IOException
    {
        Environment env = mock(Environment.class);
        KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
        KeyPair oauthKeyPair = gen.generateKeyPair();
        StringWriter publicKeyWriter = new StringWriter();
        PEMWriter pubWriter = new PEMWriter(publicKeyWriter);
        pubWriter.writeObject(oauthKeyPair.getPublic());
        pubWriter.close();

        StringWriter privateKeyWriter = new StringWriter();
        PEMWriter privWriter = new PEMWriter(privateKeyWriter);
        privWriter.writeObject(oauthKeyPair.getPrivate());
        privWriter.close();

        when(env.getEnv("OAUTH_LOCAL_PUBLIC_KEY")).thenReturn(publicKeyWriter.toString());
        when(env.getEnv("OAUTH_LOCAL_PRIVATE_KEY")).thenReturn(privateKeyWriter.toString());
        when(env.getEnv("OAUTH_LOCAL_KEY")).thenReturn(appKey);
        return new RunnerSignedRequestHandler(appKey, env);
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
