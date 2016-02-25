package com.atlassian.plugin.connect.test.common.servlet;

import com.samskivert.mustache.Mustache;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.Map;

/**
 *
 */
public class HttpUtils {
    public static void renderHtml(HttpServletResponse resp, String template, Map<String, Object> context) throws IOException {
        renderWithContentType(resp, "text/html", template, context);
    }

    public static void renderWithContentType(HttpServletResponse resp, String contentType, String template, Map<String, Object> context) throws IOException {
        resp.setContentType(contentType);
        byte[] bytes = render(template, context).getBytes(Charset.forName("UTF-8"));
        resp.setContentLength(bytes.length);
        resp.getOutputStream().write(bytes);
        resp.getOutputStream().close();
    }

    public static String render(String template, Map<String, Object> context) {
        StringWriter writer = new StringWriter();
        InputStream resourceAsStream = null;
        try {
            resourceAsStream = HttpUtils.class.getClassLoader().getResourceAsStream(template);
            Mustache.compiler().compile(
                    new InputStreamReader(resourceAsStream)).execute(context,
                    writer);
        } finally {
            try {
                resourceAsStream.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return writer.toString();
    }
}
