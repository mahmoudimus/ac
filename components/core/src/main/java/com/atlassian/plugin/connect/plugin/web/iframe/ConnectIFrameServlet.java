package com.atlassian.plugin.connect.plugin.web.iframe;

import java.io.IOException;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atlassian.plugin.connect.api.web.context.ModuleContextParameters;
import com.atlassian.plugin.connect.api.web.iframe.IFrameRenderStrategy;
import com.atlassian.plugin.connect.api.web.iframe.IFrameRenderStrategyRegistry;
import com.atlassian.plugin.connect.plugin.web.context.ModuleContextParser;

import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;

/**
 * Renders an iframe targeting an endpoint provided by a Connect addon.
 */
public class ConnectIFrameServlet extends HttpServlet
{

    private static final String CLASSIFIER_PARAMETER = "classifier";

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
                    Optional<String> moduleUiParameters = moduleUiParamParser.parseUiParameters(req);
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
        String classifier = req.getParameter(CLASSIFIER_PARAMETER);
        String lookupClassifier = IFrameRenderStrategyRegistry.JSON_CLASSIFIER.equals(classifier) ? null : classifier;

        IFrameRenderStrategy renderStrategy = IFrameRenderStrategyRegistry.get(addOnKey, moduleKey, lookupClassifier);

        if (null != renderStrategy && IFrameRenderStrategyRegistry.JSON_CLASSIFIER.equals(classifier))
        {
            return renderStrategy.toJsonRenderStrategy();
        }

        return renderStrategy;
    }
}
