package com.atlassian.plugin.connect.plugin.redirect;

import com.atlassian.fugue.Option;
import com.atlassian.plugin.connect.api.iframe.context.ModuleContextParameters;
import com.atlassian.plugin.connect.api.iframe.render.uri.IFrameUriBuilderFactory;
import com.atlassian.plugin.connect.plugin.iframe.context.ModuleContextParser;
import com.atlassian.plugin.connect.plugin.iframe.context.ModuleUiParamParser;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategyBuilderImpl;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;

/**
 * Creates signed url to the add-on for given reqest and return it as redirection.
 */
public class RedirectServlet extends HttpServlet
{
    // It matches addOnKey and moduleKey from url
    private static final Pattern PATH_PATTERN = Pattern.compile("^/([^/]+)/([^/]+)");

    public static final int TEMPORARY_REDIRECT_CODE = 307;
    public static final int REDIRECT_CACHE_TIME = 120;

    private final RedirectRegistry redirectRegistry;
    private final ModuleContextParser moduleContextParser;
    private final ModuleUiParamParser moduleUiParamParser;
    private final IFrameUriBuilderFactory iFrameUriBuilderFactory;
    private final TemplateRenderer templateRenderer;

    public RedirectServlet(RedirectRegistry redirectRegistry,
            ModuleContextParser moduleContextParser,
            ModuleUiParamParser moduleUiParamParser,
            IFrameUriBuilderFactory iFrameUriBuilderFactory,
            TemplateRenderer templateRenderer)
    {
        this.redirectRegistry = redirectRegistry;
        this.moduleContextParser = moduleContextParser;
        this.moduleUiParamParser = moduleUiParamParser;
        this.iFrameUriBuilderFactory = iFrameUriBuilderFactory;
        this.templateRenderer = templateRenderer;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException
    {
        Matcher matcher = PATH_PATTERN.matcher(req.getPathInfo());
        if (!matcher.find())
        {
            resp.sendError(SC_NOT_FOUND);
        }

        String addOnKey = matcher.group(1);
        String moduleKey = matcher.group(2);

        RedirectData redirectData = redirectRegistry.get(addOnKey, moduleKey);
        if (redirectData == null)
        {
            resp.sendError(SC_NOT_FOUND);
        }

        ModuleContextParameters moduleContextParameters = moduleContextParser.parseContextParameters(req);
        if (redirectData.shouldRedirect(moduleContextParameters))
        {
            Option<String> moduleUiParameters = moduleUiParamParser.parseUiParameters(req);
            String signedUrl = iFrameUriBuilderFactory.builder()
                    .addOn(addOnKey)
                    .namespace(moduleKey)
                    .urlTemplate(redirectData.getUrlTemplate())
                    .context(moduleContextParameters)
                    .uiParams(moduleUiParameters)
                    .dialog(false) // urls used in dialog are created in ConnectIframeServlet
                    .sign(true)
                    .build();

            redirect(resp, signedUrl);
        }
        else
        {
            rendedAccessDenied(resp, redirectData);
        }
    }

    private void redirect(HttpServletResponse resp, String url)
    {
        resp.setStatus(TEMPORARY_REDIRECT_CODE);
        resp.setHeader("location", url);
        resp.setHeader("cache-control", "private, max-age=" + REDIRECT_CACHE_TIME);
    }

    private void rendedAccessDenied(HttpServletResponse resp, RedirectData redirectData)
            throws IOException
    {
        Map<String, Object> renderContext = ImmutableMap.<String, Object>builder()
                .put("title", StringUtils.defaultIfEmpty(redirectData.getTitle(), ""))
                .put("decorator", IFrameRenderStrategyBuilderImpl.ATL_GENERAL)
                .build();

        templateRenderer.render(redirectData.getAccessDeniedTemplate(), renderContext, resp.getWriter());
    }
}
