package com.atlassian.plugin.connect.plugin.web.iframe;

import com.atlassian.plugin.connect.api.web.iframe.IFrameRenderStrategy;
import com.atlassian.plugin.connect.api.web.iframe.IFrameRenderStrategyRegistry;
import com.atlassian.plugin.connect.plugin.util.KeysFromPathMatcher;
import com.atlassian.plugin.connect.plugin.util.KeysFromPathMatcher.AddOnKeyAndModuleKey;
import com.atlassian.plugin.connect.plugin.web.context.ModuleContextParser;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static com.atlassian.plugin.connect.api.web.iframe.IFrameRenderStrategyRegistry.JSON_CLASSIFIER;
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;

/**
 * Renders an iframe targeting an endpoint provided by a Connect addon.
 */
public class ConnectIFrameServlet extends HttpServlet
{

    private static final String CLASSIFIER_PARAMETER = "classifier";

    private final IFrameRenderStrategyRegistry IFrameRenderStrategyRegistry;
    private final ModuleContextParser moduleContextParser;
    private final ModuleUiParamParser moduleUiParamParser;
    private final KeysFromPathMatcher keysFromPathMatcher;

    public ConnectIFrameServlet(IFrameRenderStrategyRegistry IFrameRenderStrategyRegistry,
            ModuleContextParser moduleContextParser,
            ModuleUiParamParser moduleUiParamParser,
            KeysFromPathMatcher keysFromPathMatcher)
    {
        this.IFrameRenderStrategyRegistry = IFrameRenderStrategyRegistry;
        this.moduleContextParser = moduleContextParser;
        this.moduleUiParamParser = moduleUiParamParser;
        this.keysFromPathMatcher = keysFromPathMatcher;
    }

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp)
            throws ServletException, IOException
    {
        Optional<AddOnKeyAndModuleKey> keys = keysFromPathMatcher.getAddOnKeyAndModuleKey(req.getPathInfo());
        if (!keys.isPresent())
        {
            resp.sendError(SC_NOT_FOUND);
            return;
        }

        IFrameRenderStrategy renderStrategy = getiFrameRenderStrategyForJsonModule(req, keys.get().getAddOnKey(), keys.get().getModuleKey());

        if (renderStrategy != null)
        {
            resp.setContentType(renderStrategy.getContentType());

            if (renderStrategy.shouldShow(Collections.emptyMap()))
            {
                Map<String, String> moduleContextParameters = moduleContextParser.parseContextParameters(req);
                Optional<String> moduleUiParameters = moduleUiParamParser.parseUiParameters(req);
                renderStrategy.render(moduleContextParameters, resp.getWriter(), moduleUiParameters);
            }
            else
            {
                renderStrategy.renderAccessDenied(resp.getWriter());
            }
        }
    }

    private IFrameRenderStrategy getiFrameRenderStrategyForJsonModule(final HttpServletRequest req, final String addonKey, final String moduleKey)
    {
        String classifier = req.getParameter(CLASSIFIER_PARAMETER);
        String lookupClassifier = JSON_CLASSIFIER.equals(classifier) ? null : classifier;

        IFrameRenderStrategy renderStrategy = IFrameRenderStrategyRegistry.get(addonKey, moduleKey, lookupClassifier);

        if (null != renderStrategy && JSON_CLASSIFIER.equals(classifier))
        {
            return renderStrategy.toJsonRenderStrategy();
        }

        return renderStrategy;
    }
}
