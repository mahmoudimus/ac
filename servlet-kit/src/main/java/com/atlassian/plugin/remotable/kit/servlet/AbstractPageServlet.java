package com.atlassian.plugin.remotable.kit.servlet;

import com.atlassian.plugin.remotable.api.annotation.ServiceReference;
import com.atlassian.plugin.remotable.api.service.RenderContext;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.atlassian.plugin.util.PluginUtils;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.atlassian.plugin.remotable.spi.util.Strings.dasherize;
import static com.atlassian.plugin.remotable.spi.util.Strings.removeSuffix;
import static com.google.common.collect.Maps.newHashMap;

public abstract class AbstractPageServlet extends HttpServlet implements InitializingBean
{
    @Inject
    @ServiceReference
    protected TemplateRenderer templateRenderer;
    @Inject
    @ServiceReference
    protected RenderContext renderContext;
    @Inject
    @ServiceReference
    protected PluginRetrievalService pluginRetrievalService;

    private String resourceBaseName;
    private Aui.Version auiVersion;
    private List<String> appStylesheetUrls;
    private List<String> appScriptUrls;

    private final boolean devMode = Boolean.getBoolean(PluginUtils.ATLASSIAN_DEV_MODE);
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private String headTemplate;
    private String tailTemplate;

    @Override
    @PostConstruct
    public void afterPropertiesSet() throws Exception
    {
        resourceBaseName = dasherize(removeSuffix(getClass().getSimpleName(), "Servlet"));

        Aui aui = getClass().getAnnotation(Aui.class);
        auiVersion = aui != null ? aui.value() : null;
        if (aui != null && auiVersion == null)
        {
            throw new IllegalStateException("Aui annotation must declare a version value");
        }

        if (!devMode)
        {
            // build and cache resolved resources in production mode
            appStylesheetUrls = getAppStylesheetUrls();
            appScriptUrls = getAppScriptUrls();
            headTemplate = getTemplate("head");
            tailTemplate = getTemplate("tail");
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

    protected void render(HttpServletRequest req, HttpServletResponse res, Map<String, ?> context) throws IOException
    {
        render(resourceBaseName, req, res, context);
    }

    protected void render(String view, HttpServletRequest req, HttpServletResponse res, Map<String, ?> context) throws IOException
    {
        renderWithLayout(resolveLayout(), view, req, res, context);
    }

    protected void renderWithLayout(String layout, String view, HttpServletRequest req, HttpServletResponse res, Map<String, ?> context) throws IOException
    {
        StringWriter body = new StringWriter();
        renderView(view, body, ImmutableMap.<String, Object>builder()
                .putAll(getBaseContext())
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

    protected void sendWithLayout(String layout, String bodyHtml, HttpServletRequest req, HttpServletResponse res, Map<String, ?> context) throws IOException
    {
        res.setStatus(200);
        res.setContentType("text/html; charset=UTF-8");

        if (layout != null)
        {
            renderView(layout, res.getWriter(), ImmutableMap.<String, Object>builder()
                    .putAll(getBaseContext())
                    .putAll(context)
                    .put("bodyHtml", bodyHtml)
                    .build());
        }
        else
        {
            res.getWriter().write(bodyHtml);
        }
    }

    protected Map<String, Object> getBaseContext()
    {
        ImmutableMap.Builder<String, Object> builder = ImmutableMap.<String, Object>builder()
            .putAll(renderContext.toContextMap())
            .put("appStylesheetUrls", devMode ? getAppStylesheetUrls() : appStylesheetUrls)
            .put("appScriptUrls", devMode ? getAppScriptUrls() : appScriptUrls)
            .put("headTemplate", devMode ? getTemplate("head") : headTemplate)
            .put("tailTemplate", devMode ? getTemplate("tail") : tailTemplate);
        if (auiVersion != null) builder.put("auiVersion", auiVersion);
        return builder.build();
    }

    private String getTemplate(String type)
    {
        String prefix = "layout-" + type + "-";
        String tmpl = null;
        if (auiVersion != null)
        {
            tmpl = getViewPath(prefix + "aui-" + auiVersion);
            if (!hasResource(tmpl))
            {
                tmpl = getViewPath(prefix + "aui");
                if (!hasResource(tmpl))
                {
                    tmpl = null;
                }
            }
        }
        if (tmpl == null)
        {
            tmpl = getViewPath(prefix + "default");
        }
        return tmpl;
    }

    protected boolean hasResource(String path)
    {
        return pluginRetrievalService.getPlugin().getResource(path) != null;
    }

    protected boolean isDevMode()
    {
        return devMode;
    }

    protected List<String> getAppStylesheetUrls()
    {
        return getAppResourceUrls(AppStylesheets.class, "stylesheet", "css");
    }

    protected List<String> getAppScriptUrls()
    {
        return getAppResourceUrls(AppScripts.class, "script", "js");
    }

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
    private List<String> getAppResourceUrls(Class<? extends Annotation> type, String typeName, String typeExt)
    {
        ImmutableList.Builder<String> urls = ImmutableList.builder();
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
                                resolveResourcePath(value, typeName, typeExt, urls, true);
                            }
                        }
                    }
                }
                else
                {
                    resolveResourcePath(resourceBaseName, typeName, typeExt, urls, true);
                }
            }
        }
        if (noMatches)
        {
            resolveResourcePath(resourceBaseName, typeName, typeExt, urls, false);
        }
        return urls.build();
    }

    // scans resources/public for the desired resource base name, trying public/<ext> and then public/
    // while preferring minified versions (identified by either -min or .min sub-extensions) in prod mode;
    // minifcation and aggregation concerns are left to the app itself
    private void resolveResourcePath(String name, String type, String ext, ImmutableList.Builder<String> urls, boolean warn)
    {
        String url = null;
        if (name.startsWith("//") || name.startsWith("http:") || name.startsWith("https:"))
        {
            url = name;
            urls.add(url);
            logger.debug("Added " + type + " resource with absolute url '" + url + "'");
        }
        else
        {
            ImmutableList.Builder<String> builder = ImmutableList.builder();
            if (!devMode) builder.addAll(minifiedCandidates(name, ext)); // prefer min in prod mode
            builder.add(getResourcePath(name, ext));
            if (devMode) builder.addAll(minifiedCandidates(name, ext)); // prefer normal in dev mode, but fallback to min
            List<String> candidates = builder.build();
            for (String candidate : candidates)
            {
                String publicPath = "public/" + candidate;
                if (hasResource(publicPath))
                {
                    url = publicPath;
                    break;
                }
            }
            if (url != null)
            {
                urls.add(url);
                logger.debug("Found " + type + " resource for name '" + name + "' at path 'public/" + url + "'");
            }
            else if (warn)
            {
                logger.warn("No " + type + " resource found for name " + name + " (tried " + Joiner.on(", ").join(candidates) + ")");
            }
        }
    }

    private String getResourcePath(String... parts)
    {
        if (parts.length == 1)
        {
            return parts[0];
        }
        else
        {
            return Joiner.on('/').join(Arrays.copyOfRange(parts, 0, parts.length - 1)) + '.' + parts[parts.length - 1];
        }
    }

    private List<String> minifiedCandidates(String name, String ext)
    {
        return ImmutableList.of(
            getResourcePath(name + ".min", ext),
            getResourcePath(name + "-min", ext)
        );
    }
}
