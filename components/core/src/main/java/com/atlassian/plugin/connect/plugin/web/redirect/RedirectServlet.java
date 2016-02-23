package com.atlassian.plugin.connect.plugin.web.redirect;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atlassian.plugin.connect.api.web.context.ModuleContextParameters;
import com.atlassian.plugin.connect.api.web.iframe.ConnectUriFactory;
import com.atlassian.plugin.connect.api.web.redirect.RedirectData;
import com.atlassian.plugin.connect.api.web.redirect.RedirectRegistry;
import com.atlassian.plugin.connect.modules.util.ModuleKeyUtils;
import com.atlassian.plugin.connect.plugin.util.KeysFromPathMatcher;
import com.atlassian.plugin.connect.plugin.web.context.ModuleContextParser;
import com.atlassian.plugin.connect.plugin.web.iframe.IFrameRenderStrategyBuilderImpl;
import com.atlassian.plugin.connect.plugin.web.iframe.ModuleUiParamParser;
import com.atlassian.templaterenderer.TemplateRenderer;

import com.google.common.collect.ImmutableMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;

import static com.atlassian.plugin.connect.modules.util.ModuleKeyUtils.addonAndModuleKey;
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;

/**
 * Creates signed url to the add-on for given request and return it as redirection.
 *
 * Note, that it does not support redirection from the module that requires the uniqueNamespace.
 */
public class RedirectServlet extends HttpServlet {
    private static final String REDIRECT_CACHE_TIME_PROPERTY = "com.atlassian.connect.redirect.cache_time";
    private static final long REDIRECT_CACHE_TIME_DEFAULT_DEFAULT = 120;
    private static final long REDIRECT_CACHE_TIME = Long.getLong(REDIRECT_CACHE_TIME_PROPERTY, REDIRECT_CACHE_TIME_DEFAULT_DEFAULT);

    private final RedirectRegistry redirectRegistry;
    private final ModuleContextParser moduleContextParser;
    private final ModuleUiParamParser moduleUiParamParser;
    private final ConnectUriFactory connectUriFactory;
    private final TemplateRenderer templateRenderer;
    private final KeysFromPathMatcher keysFromPathMatcher;

    public RedirectServlet(RedirectRegistry redirectRegistry,
                           ModuleContextParser moduleContextParser,
                           ModuleUiParamParser moduleUiParamParser,
                           ConnectUriFactory connectUriFactory,
                           TemplateRenderer templateRenderer,
                           KeysFromPathMatcher keysFromPathMatcher) {
        this.redirectRegistry = redirectRegistry;
        this.moduleContextParser = moduleContextParser;
        this.moduleUiParamParser = moduleUiParamParser;
        this.connectUriFactory = connectUriFactory;
        this.templateRenderer = templateRenderer;
        this.keysFromPathMatcher = keysFromPathMatcher;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Optional<KeysFromPathMatcher.AddOnKeyAndModuleKey> keys = keysFromPathMatcher.getAddOnKeyAndModuleKey(req.getPathInfo());
        if (!keys.isPresent()) {
            resp.sendError(SC_NOT_FOUND);
            return;
        }

        Optional<RedirectData> redirectDataOpt = redirectRegistry.get(keys.get().getAddOnKey(), keys.get().getModuleKey());
        if (!redirectDataOpt.isPresent()) {
            resp.sendError(SC_NOT_FOUND);
            return;
        }
        RedirectData redirectData = redirectDataOpt.get();

        ModuleContextParameters moduleContextParameters = moduleContextParser.parseContextParameters(req);
        if (redirectData.shouldRedirect(Collections.<String, Object>unmodifiableMap(moduleContextParameters))) {
            Optional<String> moduleUiParameters = moduleUiParamParser.parseUiParameters(req);
            String signedUrl = connectUriFactory.createConnectAddonUriBuilder()
                    .addon(keys.get().getAddOnKey())
                    .namespace(ModuleKeyUtils.addonAndModuleKey(keys.get().getAddOnKey(), keys.get().getModuleKey()))
                    .urlTemplate(redirectData.getUrlTemplate())
                    .context(moduleContextParameters)
                    .uiParams(moduleUiParameters)
                    .dialog(false) // ifreams that appears in dialogs use ConnectIframeServlet
                    .sign(true)
                    .build();

            redirect(resp, signedUrl);
        } else {
            renderAccessDenied(resp, redirectData);
        }
    }

    private void redirect(HttpServletResponse resp, String url) {
        resp.setStatus(HttpStatus.SC_TEMPORARY_REDIRECT);
        resp.setHeader("location", url);

        // response will be cached for given time, only on client side (no proxies)
        // and after that response must be revalidated (usually it's default behaviour but the "must-revalidate" param makes this unambiguous)
        resp.setHeader("cache-control", "private, must-revalidate, max-age=" + REDIRECT_CACHE_TIME);
    }

    private void renderAccessDenied(HttpServletResponse resp, RedirectData redirectData)
            throws IOException {
        Map<String, Object> renderContext = ImmutableMap.<String, Object>builder()
                .put("title", StringUtils.defaultIfEmpty(redirectData.getTitle(), ""))
                .put("decorator", IFrameRenderStrategyBuilderImpl.ATL_GENERAL)
                .build();

        resp.setStatus(HttpStatus.SC_NOT_FOUND);
        templateRenderer.render(redirectData.getAccessDeniedTemplate(), renderContext, resp.getWriter());
    }
}
