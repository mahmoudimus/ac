package com.atlassian.plugin.connect.plugin.iframe;

import com.atlassian.plugin.connect.plugin.iframe.context.ModuleContextParameters;
import com.atlassian.plugin.connect.plugin.iframe.context.ModuleContextParser;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategy;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategyRegistry;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
            String addonKey = matcher.group(1);
            String moduleKey = matcher.group(2);
            IFrameRenderStrategy renderStrategy = IFrameRenderStrategyRegistry.get(addonKey, moduleKey);
            if (renderStrategy != null)
            {
                ModuleContextParameters moduleContextParameters = moduleContextParser.parseContextParameters(req);
                resp.setContentType("text/html");
                renderStrategy.render(moduleContextParameters, resp.getOutputStream());
                return;
            }
        }

        resp.sendError(SC_NOT_FOUND);
    }

}
