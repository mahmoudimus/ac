package com.atlassian.labs.remoteapps.kit.servlet;

import com.atlassian.labs.remoteapps.api.annotation.ServiceReference;
import com.atlassian.labs.remoteapps.api.service.RequestContext;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.atlassian.plugin.util.PluginUtils;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.sal.api.message.LocaleResolver;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.atlassian.labs.remoteapps.spi.util.Strings.dasherize;
import static com.atlassian.labs.remoteapps.spi.util.Strings.removeSuffix;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;

public abstract class AbstractPageServlet extends HttpServlet
{
    @ServiceReference
    protected RequestContext requestContext;
    @ServiceReference
    protected TemplateRenderer templateRenderer;
    @ServiceReference
    protected I18nResolver i18nResolver;
    @ServiceReference
    protected LocaleResolver localeResolver;
    @ServiceReference
    protected PluginRetrievalService pluginRetrievalService;

    private final String resourceBaseName;
    private List<String> appStylesheetUrls;
    private List<String> appScriptUrls;

    private final boolean devMode = Boolean.getBoolean(PluginUtils.ATLASSIAN_DEV_MODE);
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    protected AbstractPageServlet()
    {
        resourceBaseName = dasherize(removeSuffix(getClass().getSimpleName(), "Servlet"));
        if (!devMode)
        {
            // build and cache resolved resources in production mode
            appStylesheetUrls = getAppStylesheetPaths();
            appScriptUrls = getAppScriptPaths();
        }
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
    {
        try
        {
            super.service(req, res);
        }
        catch (Exception e)
        {
            StringWriter error = new StringWriter();
            e.printStackTrace(new PrintWriter(error));
            renderError(error.toString(), req, res);
        }
    }

    protected void render(HttpServletRequest req, HttpServletResponse res, Map<String, Object> context) throws IOException
    {
        render(resourceBaseName, req, res, context);
    }

    protected void render(String view, HttpServletRequest req, HttpServletResponse res, Map<String, Object> context) throws IOException
    {
        renderWithLayout(resolveLayout(), view, req, res, context);
    }

    protected void renderWithLayout(String layout, String view, HttpServletRequest req, HttpServletResponse res, Map<String, Object> context) throws IOException
    {
        StringWriter body = new StringWriter();
        renderView(view, body, ImmutableMap.<String, Object>builder()
            .putAll(getBaseContext(req))
            .putAll(context)
            .build());
        sendWithLayout(layout, body.toString(), req, res, context);
    }

    protected void renderError(String error, HttpServletRequest req, HttpServletResponse res) throws IOException
    {
        renderErrorWithLayout(resolveLayout(), error, req, res);
    }

    protected void renderErrorWithLayout(String layout, String error, HttpServletRequest req, HttpServletResponse res) throws IOException
    {
        Map<String, Object> context = newHashMap();
        context.put("error", error);
        renderWithLayout(layout, "error", req, res, context);
    }

    protected void send(String bodyHtml, HttpServletRequest req, HttpServletResponse res) throws IOException
    {
        sendWithLayout(resolveLayout(), bodyHtml, req, res, Maps.<String, Object>newHashMap());
    }

    protected void sendWithLayout(String layout, String bodyHtml, HttpServletRequest req, HttpServletResponse res, Map<String, Object> context) throws IOException
    {
        res.setStatus(200);
        res.setContentType("text/html; charset=UTF-8");

        if (layout != null)
        {
            renderView(layout, res.getWriter(), ImmutableMap.<String, Object>builder()
                .putAll(getBaseContext(req))
                .putAll(context)
                .put("bodyHtml", bodyHtml)
                .build());
        }
        else
        {
            res.getWriter().write(bodyHtml);
        }
    }

    protected Map<String, Object> getBaseContext(HttpServletRequest req)
    {
        String hostBaseUrl = requestContext.getHostBaseUrl();
        Locale locale = localeResolver.getLocale(req);
        return ImmutableMap.<String, Object>builder()
            .put("hostContextPath", URI.create(hostBaseUrl).getPath())
            .put("hostBaseUrl", hostBaseUrl)
            .put("hostStylesheetUrl", hostBaseUrl + "/remoteapps/all.css")
            .put("hostScriptUrl", hostBaseUrl + "/remoteapps/all.js")
            .put("appStylesheetPaths", devMode ? getAppStylesheetPaths() : appStylesheetUrls)
            .put("appScriptPaths", devMode ? getAppScriptPaths() : appScriptUrls)
            .put("userId", requestContext.getUserId())
            .put("clientKey", requestContext.getClientKey())
            .put("i18n", i18nResolver)
            .put("locale", locale)
            .build();
    }

    protected boolean hasResource(String path)
    {
        return pluginRetrievalService.getPlugin().getResource(path) != null;
    }

    protected boolean isDevMode()
    {
        return devMode;
    }

    protected List<String> getAppStylesheetPaths()
    {
        return getAppResourcePaths(AppStylesheets.class, "stylesheet", "css");
    }

    protected List<String> getAppScriptPaths()
    {
        return getAppResourcePaths(AppScripts.class, "script", "js");
    }

// @todo joda-time not currently available in container
//    protected DateTimeFormatter getDateTimeFormatter(String pattern, HttpServletRequest req)
//    {
//        return DateTimeFormat.forPattern(pattern).withLocale(localeResolver.getLocale(req));
//    }

    private String getViewPath(String view)
    {
        return "views/" + view + ".vm";
    }

    private String resolveLayout()
    {
        String servletLayout = resourceBaseName + "-layout";
        return hasResource(getViewPath(servletLayout)) ? servletLayout : "layout";
    }

    private void renderView(String view, Writer writer, Map<String, Object> context) throws IOException
    {
        String viewPath = getViewPath(view);
        if (!hasResource(viewPath))
        {
            throw new IllegalStateException("No view resource found for path " + viewPath);
        }
        templateRenderer.render(viewPath, context, writer);
    }

    // scans AppStylesheets and AppScripts annotations to build a list of resource paths
    private List<String> getAppResourcePaths(Class<? extends Annotation> type, String typeName, String typeExt)
    {
        ImmutableList.Builder<String> paths = ImmutableList.builder();
        Annotation[] annotations = getClass().getAnnotations();
        boolean noMatches = true;

        for (Annotation annotation : annotations)
        {
            String[] values;

            if (annotation.annotationType() == type)
            {
                noMatches = false;
                if (annotation instanceof AppStylesheets)
                {
                    values = ((AppStylesheets) annotation).value();
                }
                else if (annotation instanceof AppScripts)
                {
                    values = ((AppScripts) annotation).value();
                }
                else
                {
                    throw new IllegalStateException("Unexpected resource annotation type: "
                        + annotation.annotationType().getSimpleName());
                }

                if (values != null)
                {
                    for (String value : values)
                    {
                        if (value != null)
                        {
                            value = value.trim();
                            if (value.length() > 0)
                            {
                                resolveResourcePath(value, typeName, typeExt, paths, true);
                            }
                        }
                    }
                }
                else
                {
                    resolveResourcePath(resourceBaseName, typeName, typeExt, paths, true);
                }
            }
        }
        if (noMatches)
        {
            resolveResourcePath(resourceBaseName, typeName, typeExt, paths, false);
        }
        return paths.build();
    }

    // scans resources/public for the desired resource base name, trying public/<ext> and then public/
    // while preferring minified versions (identified by either -min or .min sub-extensions) in prod mode;
    // minifcation and aggregation concerns are left to the app itself
    private void resolveResourcePath(String name, String type, String ext, ImmutableList.Builder<String> paths, boolean warn)
    {
        String path = null;
        List<String> candidates = newArrayList();
        if (!devMode)
        {
            candidates.add(getResourcePath(ext, name + ".min", ext));
            candidates.add(getResourcePath(ext, name + "-min", ext));
            candidates.add(getResourcePath(name + ".min", ext));
            candidates.add(getResourcePath(name + "-min", ext));
        }
        candidates.add(getResourcePath(ext, name, ext));
        candidates.add(getResourcePath(name, ext));
        for (String candidate : candidates)
        {
            String publicPath = "public/" + candidate;
            if (hasResource(publicPath))
            {
                path = publicPath;
                break;
            }
        }
        if (path != null)
        {
            paths.add(path);
            logger.debug("Found " + type + " resource for name '" + name + "' at path public/" + path);
        }
        else if (warn)
        {
            logger.warn("No " + type + " found for resource " + name + " (tried " + Joiner.on(", ").join(candidates) + ")");
        }
    }

    private String getResourcePath(String... parts)
    {
        if (parts.length == 1) return parts[0];
        else return Joiner.on('/').join(Arrays.copyOfRange(parts, 0, parts.length - 1)) + '.' + parts[parts.length - 1];
    }
}
