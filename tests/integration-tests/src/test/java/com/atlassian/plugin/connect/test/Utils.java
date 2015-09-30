package com.atlassian.plugin.connect.test;

import com.atlassian.plugin.connect.test.server.RunnerSignedRequestHandler;
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
import java.util.Map;

import static com.atlassian.plugin.connect.spi.util.XmlUtils.createSecureSaxReader;
import static com.google.common.collect.Maps.newHashMap;

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

    public static RunnerSignedRequestHandler createSignedRequestHandler(String appKey) throws NoSuchAlgorithmException, IOException
    {
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

        Environment env = new TestEnv();
        env.setEnv("OAUTH_LOCAL_PUBLIC_KEY", publicKeyWriter.toString());
        env.setEnv("OAUTH_LOCAL_PRIVATE_KEY", privateKeyWriter.toString());
        env.setEnv("OAUTH_LOCAL_KEY", appKey);
        return new RunnerSignedRequestHandler(appKey, env);
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
        private Map<String, String> env = newHashMap();

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
