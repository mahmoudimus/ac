package com.atlassian.labs.remoteapps.modules;

import com.atlassian.labs.remoteapps.api.PermissionDeniedException;
import com.atlassian.labs.remoteapps.modules.page.IFrameContext;
import com.atlassian.labs.remoteapps.modules.page.PageInfo;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.elements.ResourceDescriptor;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.atlassian.plugin.webresource.UrlMode;
import com.atlassian.plugin.webresource.WebResourceManager;
import com.atlassian.plugin.webresource.WebResourceUrlProvider;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.atlassian.labs.remoteapps.util.EncodingUtils.escapeQuotes;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;

/**
 *
 */
@Component
public class IFrameRenderer
{
    private final TemplateRenderer templateRenderer;
    private final WebResourceManager webResourceManager;
    private final ApplicationProperties applicationProperties;
    private final WebResourceUrlProvider webResourceUrlProvider;
    private final Plugin plugin;

    @Autowired
    public IFrameRenderer(TemplateRenderer templateRenderer,
                          WebResourceManager webResourceManager,
                          ApplicationProperties applicationProperties,
                          WebResourceUrlProvider webResourceUrlProvider,
                          PluginRetrievalService pluginRetrievalService)
    {
        this.templateRenderer = templateRenderer;
        this.webResourceManager = webResourceManager;
        this.applicationProperties = applicationProperties;
        this.webResourceUrlProvider = webResourceUrlProvider;
        this.plugin = pluginRetrievalService.getPlugin();
    }

    public String render(IFrameContext iframeContext, String remoteUser) throws IOException
    {
        return render(iframeContext, "", Collections.<String, String[]>emptyMap(), remoteUser);
    }

    public void renderPage(IFrameContext iframeContext, PageInfo pageInfo, String extraPath, Map<String, String[]> queryParams, String remoteUser, Writer writer) throws IOException
    {
        try
        {
            if (!pageInfo.getCondition().shouldDisplay(Collections.<String, Object>emptyMap()))
            {
                throw new PermissionDeniedException();
            }

            Map<String, Object> ctx = newHashMap(iframeContext.getIFrameParams().getAsMap());
            if (!ctx.containsKey("width") && queryParams.get("width") != null)
            {
                iframeContext.getIFrameParams().setParam("width", queryParams.get("width")[0]);
            }
            if (!ctx.containsKey("height") && queryParams.get("height") != null)
            {
                iframeContext.getIFrameParams().setParam("height", queryParams.get("height")[0]);
            }

            ctx.put("title", pageInfo.getTitle());
            ctx.put("contextPath", getContextPath());
            ctx.put("iframeHtml",
                    render(iframeContext, extraPath, queryParams,
                            remoteUser));
            ctx.put("decorator", pageInfo.getDecorator());

            templateRenderer.render("velocity/iframe-page" + pageInfo.getTemplateSuffix() + ".vm",
                    ctx, writer);
        } catch (PermissionDeniedException ex)
        {
            templateRenderer.render(
                    "velocity/iframe-page-accessdenied" + pageInfo.getTemplateSuffix() + ".vm",
                    ImmutableMap.<String, Object>of(
                            "title", pageInfo.getTitle(),
                            "decorator", pageInfo.getDecorator()), writer);
        }
    }

    private String getContextPath()
    {
        String baseUrl = applicationProperties.getBaseUrl();
        return baseUrl != null ? URI.create(baseUrl).getPath() : "";
    }

    public String render(IFrameContext iframeContext, String extraPath, Map<String, String[]> queryParams, String remoteUser) throws IOException
    {
        webResourceManager.requireResourcesForContext("remoteapps-iframe");

        URI hostUri = URI.create(applicationProperties.getBaseUrl());
        String host = hostUri.getScheme() + "://" + hostUri.getHost();
        if (hostUri.getPort() > 0)
        {
            host = host + ":" + hostUri.getPort();
        }
        String iframeUrl = iframeContext.getIframePath() + (extraPath != null ? extraPath : "");

        Map<String,String[]> allParams = newHashMap(queryParams);
        allParams.put("" +
                "xdm_e", new String[]{host});
        allParams.put("xdm_c", new String[]{"channel-" + iframeContext.getNamespace()});
        allParams.put("xdm_p", new String[]{"1"});
        String signedUrl = iframeContext.getLinkOps().signGetUrl(remoteUser, iframeUrl, allParams);

        // clear xdm params as they are added by easyxdm later
        signedUrl = UriBuilder.fromUri(signedUrl)
                    .replaceQueryParam("xdm_e")
                    .replaceQueryParam("xdm_c")
                    .replaceQueryParam("xdm_p").build().toString();

        Map<String,Object> ctx = newHashMap(iframeContext.getIFrameParams().getAsMap());
        ctx.put("iframeSrcHtml", escapeQuotes(signedUrl));
        ctx.put("remoteapp", iframeContext.getLinkOps().get());
        ctx.put("namespace", iframeContext.getNamespace());
        ctx.put("scriptUrls", getJavaScriptUrls());
        ctx.put("contextPath", getContextPath());
        ctx.put("userId", remoteUser == null ? "" : remoteUser);

        // even if same origin, force postMessage as same origin will have xdm_e with the full
        // url, which we don't know as we only populate host and port from the base url
        String xdmProtocol = URI.create(host).getHost().equalsIgnoreCase(URI.create(signedUrl).getHost()) ? "\"1\"" : "undefined";
        ctx.put("xdmProtocolHtml", xdmProtocol);

        StringWriter output = new StringWriter();
        templateRenderer.render("velocity/iframe-body.vm", ctx, output);
        return output.toString();
    }

    public List<String> getJavaScriptUrls()
    {
        List<String> scripts = newArrayList();
        ModuleDescriptor<?> moduleDescriptor = plugin.getModuleDescriptor("iframe-host");
        for (ResourceDescriptor descriptor : moduleDescriptor.getResourceDescriptors())
        {
            String src = webResourceUrlProvider.getStaticPluginResourceUrl(moduleDescriptor, descriptor.getName(), UrlMode.AUTO);
            scripts.add(src);
        }
        return scripts;
    }
}
