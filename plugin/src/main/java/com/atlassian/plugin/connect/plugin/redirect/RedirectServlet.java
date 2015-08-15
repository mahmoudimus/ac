package com.atlassian.plugin.connect.plugin.redirect;

import com.atlassian.fugue.Option;
import com.atlassian.plugin.connect.api.iframe.context.ModuleContextParameters;
import com.atlassian.plugin.connect.api.iframe.render.uri.IFrameUriBuilderFactory;
import com.atlassian.plugin.connect.plugin.redirect.RedirectRegistry.RedirectData;
import com.atlassian.plugin.connect.plugin.iframe.context.ModuleContextParser;
import com.atlassian.plugin.connect.plugin.iframe.context.ModuleUiParamParser;

import java.io.IOException;
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
    private static final Pattern PATH_PATTERN = Pattern.compile("^/([^/]+)/([^/]+)");
    private final RedirectRegistry redirectRegistry;
    private final ModuleContextParser moduleContextParser;
    private final ModuleUiParamParser moduleUiParamParser;
    private final IFrameUriBuilderFactory iFrameUriBuilderFactory;

    public RedirectServlet(RedirectRegistry redirectRegistry,
            ModuleContextParser moduleContextParser,
            ModuleUiParamParser moduleUiParamParser,
            IFrameUriBuilderFactory iFrameUriBuilderFactory)
    {
        this.redirectRegistry = redirectRegistry;
        this.moduleContextParser = moduleContextParser;
        this.moduleUiParamParser = moduleUiParamParser;
        this.iFrameUriBuilderFactory = iFrameUriBuilderFactory;
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
        ModuleContextParameters moduleContextParameters = moduleContextParser.parseContextParameters(req);
        Option<String> uiParameters = moduleUiParamParser.parseUiParameters(req);
        String signedUrl = iFrameUriBuilderFactory.builder()
                .addOn(addOnKey)
                .namespace(moduleKey)
                .urlTemplate(redirectData.getUrlTemplate())
                .context(moduleContextParameters)
                .uiParams(uiParameters)
                .dialog(false)
                .sign(true)
                .build();

        resp.setStatus(307); // Temporary Redirect
        resp.setHeader("location", signedUrl);
        resp.setHeader("cache-control", "private, max-age=150");
    }
}
