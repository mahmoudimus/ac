package com.atlassian.plugin.connect.plugin.module.page;

import com.atlassian.extras.common.org.springframework.util.StringUtils;
import com.atlassian.plugin.connect.plugin.module.IFrameRendererImpl;
import com.atlassian.plugin.connect.plugin.module.webfragment.UrlVariableSubstitutor;
import com.atlassian.plugin.connect.spi.module.IFrameContext;
import com.atlassian.sal.api.user.UserManager;
import com.google.common.collect.ImmutableMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A servlet that loads its content from a remote plugin's iframe
 */
public class IFramePageServlet extends HttpServlet
{
    private final UserManager userManager;
    private final UrlVariableSubstitutor urlVariableSubstitutor;
    private final PageInfo pageInfo;
    private final IFrameContext iframeContext;
    private final IFrameRendererImpl iFrameRenderer;
    private final Map<String, String> contextParamNameToSymbolicName; // e.g. "my_space_id": "space.id"

    public IFramePageServlet(PageInfo pageInfo,
            IFrameRendererImpl iFrameRenderer,
            IFrameContext iframeContext,
            UserManager userManager,
            UrlVariableSubstitutor urlVariableSubstitutor,
            Map<String, String> contextParamNameToSymbolicName)
    {
        this.iframeContext = iframeContext;
        this.iFrameRenderer = iFrameRenderer;
        this.pageInfo = pageInfo;
        this.userManager = userManager;
        this.urlVariableSubstitutor = urlVariableSubstitutor;
        this.contextParamNameToSymbolicName = checkNotNull(contextParamNameToSymbolicName);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
            IOException
    {
        PrintWriter out = resp.getWriter();
        resp.setContentType("text/html");
        String originalPath = iframeContext.getIframePath();
        String iFramePath = urlVariableSubstitutor.replace(originalPath, mapRequestParametersToContextParameters(req));

        iFrameRenderer.renderPage(
                new IFrameContextImpl(iframeContext.getPluginKey(), iFramePath, iframeContext.getNamespace(), iframeContext.getIFrameParams()),
                pageInfo, req.getPathInfo(), copyRequestContext(req, originalPath), userManager.getRemoteUsername(req), out);
    }

    /**
     * Map ( {"page_id":"${page.id}"}, {"page_id":1234} ) to {"page.id":1234}}
     * @param req Incoming {@link HttpServletRequest} containing concrete parameters and their values
     * @return {@link Map<String, Object>} suitable for sending to {@link UrlVariableSubstitutor}
     */
    private Map<String, Object> mapRequestParametersToContextParameters(HttpServletRequest req)
    {
        Map<String, String[]> parameterMap = req.getParameterMap();
        Map<String, Object> result = new HashMap<String, Object>(parameterMap.size());

        for (Map.Entry<String, String[]> paramWithValue : parameterMap.entrySet())
        {
            String requestParamName = paramWithValue.getKey(); // e.g. "space_id"

            if (contextParamNameToSymbolicName.containsKey(requestParamName))
            {
                String symbolicName = contextParamNameToSymbolicName.get(requestParamName); // e.g. "${space.id}"
                symbolicName = symbolicName.replaceAll("\\$\\{([^}]*)}", "$1"); // "${space.id}" -> "space.id"
                result.put(symbolicName, paramWithValue.getValue());
            }
        }

        return result;
    }

    private Map<String, String[]> copyRequestContext(HttpServletRequest req, String path)
    {
        Set<String> variablesUsedInPath = urlVariableSubstitutor.getContextVariableMap(path).keySet();
        ImmutableMap.Builder<String, String[]> builder = ImmutableMap.builder();
        final Set<Map.Entry<String, String[]>> requestParameters = (Set<Map.Entry<String, String[]>>) req.getParameterMap().entrySet();
        for (Map.Entry<String, String[]> entry : requestParameters)
        {
            // copy only these context parameters which aren't already a part of URL.
            if (!variablesUsedInPath.contains(entry.getKey()))
            {
                builder.put(entry.getKey(), entry.getValue());
            }
        }
        return builder.build();
    }

}
