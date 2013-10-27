package it;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atlassian.plugin.connect.test.HttpUtils;
import com.atlassian.plugin.connect.test.server.AtlassianConnectAddOnRunner;

import com.google.common.collect.ImmutableMap;

public final class MyContextAwareWebPanelServlet extends ContextServlet
{
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp, Map<String, Object> context) throws ServletException, IOException
    {
        HttpUtils.renderHtml(resp,
                "hello-world-page.mu",
                ImmutableMap.<String, Object>builder()
                            .putAll(context)
                            .build());
    }
}
