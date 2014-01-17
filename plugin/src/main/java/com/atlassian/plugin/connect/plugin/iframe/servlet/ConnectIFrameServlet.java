package com.atlassian.plugin.connect.plugin.iframe.servlet;

import com.atlassian.plugin.connect.plugin.iframe.context.ModuleContextParameters;
import com.atlassian.plugin.connect.plugin.iframe.context.ModuleContextParser;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategy;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategyRegistry;
import com.atlassian.plugin.connect.spi.PermissionDeniedException;

import java.io.IOException;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.google.common.base.Preconditions.checkNotNull;
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;

/**
 * Renders an iframe targeting an endpoint provided by a Connect addon.
 */
public class ConnectIFrameServlet extends HttpServlet
{
    private static final Pattern PATH_PATTERN = Pattern.compile("^/([^/]+)/([^/]+)");

    private final IFrameRenderStrategyRegistry IFrameRenderStrategyRegistry;
    private final ModuleContextParser moduleContextParser;

    public ConnectIFrameServlet(IFrameRenderStrategyRegistry IFrameRenderStrategyRegistry, ModuleContextParser moduleContextParser)
    {
        this.IFrameRenderStrategyRegistry = IFrameRenderStrategyRegistry;
        this.moduleContextParser = moduleContextParser;
    }

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp)
            throws ServletException, IOException
    {
        Matcher matcher = PATH_PATTERN.matcher(req.getPathInfo());
        if (matcher.find())
        {
            String addOnKey = matcher.group(1);
            String moduleKey = matcher.group(2);
            IFrameRenderStrategy renderStrategy = IFrameRenderStrategyRegistry.get(addOnKey, moduleKey);
            if (renderStrategy != null)
            {
                renderStrategy.preProcessRequest(req);

                if (!renderStrategy.shouldShow(Collections.<String, Object>emptyMap()))
                {
                    throw new PermissionDeniedException(addOnKey, "Cannot render iframe for this page.");
                }

                ModuleContextParameters moduleContextParameters = moduleContextParser.parseContextParameters(req);
                resp.setContentType("text/html");
                renderStrategy.render(moduleContextParameters, resp.getOutputStream());
                return;
            }
        }

        resp.sendError(SC_NOT_FOUND);
    }

    public static String iFrameServletPath(String addOnKey, String moduleKey)
    {
        return "/plugins/servlet/ac/" + checkNotNull(addOnKey) + "/" + checkNotNull(moduleKey);
    }

}
