package it.servlet.iframe;

import com.atlassian.plugin.connect.test.HttpUtils;
import com.google.common.collect.ImmutableMap;
import it.servlet.ContextServlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

public class MustacheServlet extends ContextServlet
{
    private final String templatePath;

    public MustacheServlet(String templatePath)
    {
        this.templatePath = templatePath;
    }

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp, Map<String, Object> context) throws ServletException, IOException
    {
        HttpUtils.renderHtml(resp, templatePath, ImmutableMap.copyOf(context));
    }
}
