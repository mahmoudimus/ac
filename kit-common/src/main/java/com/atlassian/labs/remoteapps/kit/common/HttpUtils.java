package com.atlassian.labs.remoteapps.kit.common;

import com.atlassian.labs.remoteapps.api.services.SignedRequestHandler;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.samskivert.mustache.Mustache;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Map;

/**
 *
 */
public class HttpUtils
{
    private final Plugin plugin;
    private final SignedRequestHandler signedRequestHandler;

    public HttpUtils(PluginRetrievalService pluginRetrievalService, SignedRequestHandler signedRequestHandler)
    {
        this.signedRequestHandler = signedRequestHandler;
        this.plugin = pluginRetrievalService.getPlugin();
    }
    public void renderHtml(HttpServletResponse resp, String template, Map<String, Object> context) throws IOException
    {
        resp.setContentType("text/html");
        byte[] bytes = render(template, context).getBytes(Charset.forName("UTF-8"));
        resp.setContentLength(bytes.length);
        resp.getOutputStream().write(bytes);
        resp.getOutputStream().close();
    }

    public String render(String template, Map<String,Object> context)
    {
        StringWriter writer = new StringWriter();
        InputStream resourceAsStream = null;
        try
        {
            resourceAsStream = plugin.getResourceAsStream(template);
            Mustache.compiler().compile(
                new InputStreamReader(resourceAsStream)).execute(context,
                writer);
        }
        finally
        {
            try
            {
                if (resourceAsStream != null)
                {
                    resourceAsStream.close();
                }
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        }
        return writer.toString();
    }

    public String sendSignedGet(String uri, String user)
    {
        try
        {
            URL url = new URL(uri + "?user_id=" + user);
            HttpURLConnection yc = (HttpURLConnection) url.openConnection();
            signedRequestHandler.sign(uri, "GET", user, yc);
            BufferedReader in = new BufferedReader(
                                    new InputStreamReader(
                                    yc.getInputStream()));
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null)
            {
                result.append(line);
                result.append("\n");
            }
            result.deleteCharAt(result.length() - 1);
            in.close();
            return result.toString();
        }
        catch (MalformedURLException e)
        {
            throw new RuntimeException(e);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    public int sendFailedSignedGet(String uri, String user)
    {
        HttpURLConnection yc = null;
        try
        {
            URL url = new URL(uri + "?user_id=" + user);
            yc = (HttpURLConnection) url.openConnection();
            signedRequestHandler.sign(uri, "GET", user, yc);
            BufferedReader in = new BufferedReader(
                                    new InputStreamReader(
                                    yc.getInputStream()));
            return yc.getResponseCode();
        }
        catch (MalformedURLException e)
        {
            throw new RuntimeException(e);
        }
        catch (IOException e)
        {
            try
            {
                if (yc != null)
                {
                    return yc.getResponseCode();
                }
                throw new RuntimeException("no status code");
            }
            catch (IOException e1)
            {
                throw new RuntimeException(e);
            }
        }
    }

}
