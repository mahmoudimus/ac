package servlets;

import com.atlassian.plugin.remotable.api.annotation.ComponentImport;
import com.atlassian.plugin.remotable.api.service.RequestContext;
import com.google.common.collect.ImmutableMap;

import java.io.IOException;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static services.HttpUtils.renderHtml;

@Named
public class MyContextAwareWebPanelServlet extends HttpServlet
{
    @Inject
    @ComponentImport
    private RequestContext requestContext;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException
    {
        renderHtml(res, "my-context-aware-web-panel.mu",
                buildParams(req.getParameterMap(), "issue_id", "project_id", "user_id", "space_id", "page_id")
        );
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
        servletParamBuilder.put("baseUrl", requestContext.getHostBaseUrl());
        return servletParamBuilder.build();
    }

}
