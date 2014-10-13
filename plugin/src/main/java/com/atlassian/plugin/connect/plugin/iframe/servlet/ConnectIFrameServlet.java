package com.atlassian.plugin.connect.plugin.iframe.servlet;

import com.atlassian.fugue.Option;
import com.atlassian.plugin.connect.plugin.iframe.context.ModuleContextParameters;
import com.atlassian.plugin.connect.plugin.iframe.context.ModuleContextParser;
import com.atlassian.plugin.connect.plugin.iframe.context.ModuleUiParamParser;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategy;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategyRegistry;

import java.io.IOException;
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
    public static final String CLASSIFIER = "classifier";
    public static final String RAW_CLASSIFIER = "raw";
    public static final String JSON_CLASSIFIER = "json";

    private static final Pattern PATH_PATTERN = Pattern.compile("^/([^/]+)/([^/]+)");

    private final IFrameRenderStrategyRegistry IFrameRenderStrategyRegistry;
    private final ModuleContextParser moduleContextParser;
    private final ModuleUiParamParser moduleUiParamParser;

    public ConnectIFrameServlet(IFrameRenderStrategyRegistry IFrameRenderStrategyRegistry,
            ModuleContextParser moduleContextParser,
            ModuleUiParamParser moduleUiParamParser)
    {
        this.IFrameRenderStrategyRegistry = IFrameRenderStrategyRegistry;
        this.moduleContextParser = moduleContextParser;
        this.moduleUiParamParser = moduleUiParamParser;
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

            IFrameRenderStrategy renderStrategy = getiFrameRenderStrategyForJsonModule(req, addOnKey, moduleKey);

            if (renderStrategy != null)
            {
                resp.setContentType(renderStrategy.getContentType());
                ModuleContextParameters moduleContextParameters = moduleContextParser.parseContextParameters(req);

                if (renderStrategy.shouldShow(moduleContextParameters))
                {
                    Option<String> moduleUiParameters = moduleUiParamParser.parseUiParameters(req);
                    renderStrategy.render(moduleContextParameters, resp.getWriter(), moduleUiParameters);
                }
                else
                {
                    renderStrategy.renderAccessDenied(resp.getWriter());
                }

                return;
            }
        }

        resp.sendError(SC_NOT_FOUND);
    }

    private IFrameRenderStrategy getiFrameRenderStrategyForJsonModule(final HttpServletRequest req, final String addOnKey, final String moduleKey)
    {
        String classifier = req.getParameter(CLASSIFIER);
        String lookupClassifier = JSON_CLASSIFIER.equals(classifier) ? null : classifier;

        IFrameRenderStrategy renderStrategy = IFrameRenderStrategyRegistry.get(addOnKey, moduleKey, lookupClassifier);

        if (null != renderStrategy && JSON_CLASSIFIER.equals(classifier))
        {
            return renderStrategy.toJsonRenderStrategy();
        }

        return renderStrategy;
    }

    public static String iFrameServletPath(String addOnKey, String moduleKey)
    {
        return "/plugins/servlet/ac/" + checkNotNull(addOnKey) + "/" + checkNotNull(moduleKey);
    }
}
