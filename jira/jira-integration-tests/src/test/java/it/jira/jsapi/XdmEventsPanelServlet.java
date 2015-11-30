package it.jira.jsapi;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atlassian.plugin.connect.test.common.servlet.ContextServlet;
import com.atlassian.plugin.connect.test.common.servlet.HttpUtils;

import com.google.common.collect.ImmutableMap;

public final class XdmEventsPanelServlet extends ContextServlet
{
    private final String id;

    public XdmEventsPanelServlet(String id)
    {
        this.id = id;
    }

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp, Map<String, Object> context) throws ServletException, IOException
    {
        HttpUtils.renderHtml(resp,
                "xdm-events-test.mu",
                ImmutableMap.<String, Object>builder()
                        .put("panelid", id)
                        .putAll(context)
                        .build());
    }
}
