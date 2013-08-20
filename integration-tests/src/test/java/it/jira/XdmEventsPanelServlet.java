package it.jira;

import com.atlassian.plugin.connect.test.HttpUtils;
import com.atlassian.plugin.connect.test.server.AtlassianConnectAddOnRunner;
import com.google.common.collect.ImmutableMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

public final class XdmEventsPanelServlet extends AtlassianConnectAddOnRunner.WithContextServlet
{
    private final int id;

    public XdmEventsPanelServlet(int id)
    {
        this.id = id;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp, Map<String, Object> context) throws ServletException, IOException
    {
        HttpUtils.renderHtml(resp,
                "xdm-events-test.mu",
                ImmutableMap.<String, Object>builder()
                            .put("panelid", id)
                            .putAll(context)
                            .build());
    }
}
