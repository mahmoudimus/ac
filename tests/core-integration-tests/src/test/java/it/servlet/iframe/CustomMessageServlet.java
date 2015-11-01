package it.servlet.iframe;

import com.atlassian.plugin.connect.test.HttpUtils;
import com.google.common.collect.ImmutableMap;
import it.servlet.ContextServlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

public final class CustomMessageServlet extends ContextServlet
{
    private final String message;
    private Boolean resize;

    public CustomMessageServlet(String message, Boolean resize)
    {
        this.message = message;
        this.resize = resize;
    }

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp, Map<String, Object> context) throws ServletException, IOException
    {
        HttpUtils.renderHtml(resp,
                "iframe-custom-message.mu",
                ImmutableMap.<String, Object>builder()
                        .putAll(context)
                        .put("message", message)
                        .put("resize", resize)
                        .build());
    }
}
