package com.atlassian.plugin.remotable.test;

import com.atlassian.plugin.remotable.test.server.RunnerSignedRequestHandler;
import com.google.common.collect.Maps;
import org.apache.commons.io.IOUtils;
import org.bouncycastle.openssl.PEMWriter;
import org.dom4j.Document;
import org.dom4j.DocumentException;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.ServerSocket;
import java.net.URL;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import static com.atlassian.plugin.remotable.spi.util.XmlUtils.createSecureSaxReader;
import static com.google.common.io.Closeables.closeQuietly;

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

    public static RunnerSignedRequestHandler createSignedRequestHandler(String appKey)
    {
        final KeyPair oauthKeyPair = getKeyPairGenerator("RSA").generateKeyPair();
        Environment env = new TestEnv();
        env.setEnv("OAUTH_LOCAL_PUBLIC_KEY", getKeyAsString(oauthKeyPair.getPublic()));
        env.setEnv("OAUTH_LOCAL_PRIVATE_KEY", getKeyAsString(oauthKeyPair.getPrivate()));
        env.setEnv("OAUTH_LOCAL_KEY", appKey);
        return new RunnerSignedRequestHandler(appKey, env);
    }

    private static String getKeyAsString(Key key)
    {
        StringWriter writer = new StringWriter();
        PEMWriter privWriter = null;
        try
        {
            privWriter = new PEMWriter(writer);
            privWriter.writeObject(key);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        finally
        {
            closeQuietly(privWriter);
        }
        return writer.toString();
    }

    private static KeyPairGenerator getKeyPairGenerator(String algo)
    {
        try
        {
            return KeyPairGenerator.getInstance(algo);
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new RuntimeException(e);
        }
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
        }
        catch (IOException e)
        {
            throw new RuntimeException("Error opening socket", e);
        }
        finally
        {
            if (socket != null)
            {
                try
                {
                    socket.close();
                }
                catch (IOException e)
                {
                    throw new RuntimeException("Error closing socket", e);
                }
            }
        }
    }

    private static class TestEnv implements Environment
    {
        private Map<String, String> env = Maps.newHashMap();

        @Override
        public String getEnv(String name)
        {
            return env.get(name);
        }

        @Override
        public String getOptionalEnv(String name, String def)
        {
            final String value = env.get(name);
            return value == null ? def : value;
        }

        @Override
        public void setEnv(String name, String value)
        {
            env.put(name, value);
        }

        @Override
        public void setEnvIfNull(String name, String value)
        {
            if (env.get(name) == null)
            {
                setEnv(name, value);
            }
        }
    }
}
