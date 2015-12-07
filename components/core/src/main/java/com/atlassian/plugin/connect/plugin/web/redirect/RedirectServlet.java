package com.atlassian.plugin.connect.plugin.web.redirect;

import com.atlassian.plugin.connect.api.web.context.ModuleContextParameters;
import com.atlassian.plugin.connect.api.web.iframe.IFrameUriBuilderFactory;
import com.atlassian.plugin.connect.api.web.redirect.RedirectData;
import com.atlassian.plugin.connect.api.web.redirect.RedirectRegistry;
import com.atlassian.plugin.connect.plugin.web.context.ModuleContextParser;
import com.atlassian.plugin.connect.plugin.web.iframe.IFrameRenderStrategyBuilderImpl;
import com.atlassian.plugin.connect.plugin.web.iframe.ModuleUiParamParser;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;

/**
 * Creates signed url to the add-on for given request and return it as redirection.
 *
 * Note, that it does not support redirection from the module that requires the uniqueNamespace.
 */
public class RedirectServlet extends HttpServlet
{
    // Matches addOnKey and moduleKey from url: /addon.key/module.key
    private static final Pattern PATH_PATTERN = Pattern.compile("^/([^/]+)/([^/]+)");

    public static final int TEMPORARY_REDIRECT_CODE = 307;
    private static final String REDIRECT_CACHE_TIME_PROPERTY = "com.atlassian.connect.redirect.cache_time";
    private static final long REDIRECT_CACHE_TIME_DEFAULT_DEFAULT = 120;
    private static final long REDIRECT_CACHE_TIME = Long.getLong(REDIRECT_CACHE_TIME_PROPERTY, REDIRECT_CACHE_TIME_DEFAULT_DEFAULT);

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
            return;
        }

        String addOnKey = matcher.group(1);
        String moduleKey = matcher.group(2);

        Optional<RedirectData> redirectDataOpt = redirectRegistry.get(addOnKey, moduleKey);
        if (!redirectDataOpt.isPresent())
        {
            resp.sendError(SC_NOT_FOUND);
            return;
        }
        RedirectData redirectData = redirectDataOpt.get();

        ModuleContextParameters moduleContextParameters = moduleContextParser.parseContextParameters(req);
        if (redirectData.shouldRedirect(moduleContextParameters))
        {
            Optional<String> moduleUiParameters = moduleUiParamParser.parseUiParameters(req);
            String signedUrl = iFrameUriBuilderFactory.builder()
                    .addOn(addOnKey)
                    .namespace(moduleKey)
                    .urlTemplate(redirectData.getUrlTemplate())
                    .context(moduleContextParameters)
                    .uiParams(moduleUiParameters)
                    .dialog(false) // ifreams that appears in dialogs use ConnectIframeServlet
                    .sign(true)
                    .build();

            redirect(resp, signedUrl);
        }
        else
        {
            renderAccessDenied(resp, redirectData);
        }
    }

    private void redirect(HttpServletResponse resp, String url)
    {
        resp.setStatus(TEMPORARY_REDIRECT_CODE);
        resp.setHeader("location", url);
        resp.setHeader("cache-control", "private, must-revalidate, max-age=" + REDIRECT_CACHE_TIME);
    }

    private void renderAccessDenied(HttpServletResponse resp, RedirectData redirectData)
            throws IOException
    {
        Map<String, Object> renderContext = ImmutableMap.<String, Object>builder()
                .put("title", StringUtils.defaultIfEmpty(redirectData.getTitle(), ""))
                .put("decorator", IFrameRenderStrategyBuilderImpl.ATL_GENERAL)
                .build();

        templateRenderer.render(redirectData.getAccessDeniedTemplate(), renderContext, resp.getWriter());
    }
}
