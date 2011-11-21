package com.atlassian.labs.remoteapps.sample;

import com.samskivert.mustache.Mustache;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.Map;

/**
 *
 */
public class HttpUtils
{
    public static void renderHtml(HttpServletResponse resp, String template, Map<String, Object> context) throws IOException
    {
        resp.setContentType("text/html");
        byte[] bytes = render(template, context).getBytes(Charset.forName("UTF-8"));
        resp.setContentLength(bytes.length);
        resp.getOutputStream().write(bytes);
        resp.getOutputStream().close();
    }

    public static String render(String template, Map<String,Object> context)
    {
        StringWriter writer = new StringWriter();
        Mustache.compiler().compile(
                new InputStreamReader(MyAdminServlet.class.getClassLoader().getResourceAsStream(template))).execute(context,
                writer);
        return writer.toString();
    }
}
