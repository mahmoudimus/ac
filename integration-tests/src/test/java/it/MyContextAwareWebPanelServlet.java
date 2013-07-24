package it;

import com.atlassian.plugin.remotable.test.HttpUtils;
import com.atlassian.plugin.remotable.test.server.AtlassianConnectAddOnRunner;
import com.google.common.collect.ImmutableMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

public final class MyContextAwareWebPanelServlet extends AtlassianConnectAddOnRunner.WithContextServlet
{
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp, Map<String, Object> context) throws ServletException, IOException
    {
        HttpUtils.renderHtml(resp,
                "my-context-aware-web-panel.mu",
                ImmutableMap.<String, Object>builder()
                        .putAll(context)
                        .build());
    }

    private ImmutableMap<String, Object> buildParams(final Map<String, String[]> parameterMap, final String... params)
    {
        final ImmutableMap.Builder<String, Object> servletParamBuilder = ImmutableMap.builder();
        for (final String param : params)
        {
            if (parameterMap.containsKey(param))
            {
                servletParamBuilder.put(param, parameterMap.get(param)[0]);
            }
        }
        return servletParamBuilder.build();
    }
}
