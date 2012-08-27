package com.atlassian.labs.remoteapps.plugin.module;

import com.atlassian.labs.remoteapps.plugin.RemoteAppAccessor;
import com.atlassian.labs.remoteapps.plugin.RemoteAppAccessorFactory;
import com.atlassian.labs.remoteapps.spi.PermissionDeniedException;
import com.atlassian.labs.remoteapps.plugin.module.page.IFrameContext;
import com.atlassian.labs.remoteapps.plugin.module.page.PageInfo;
import com.atlassian.labs.remoteapps.plugin.util.uri.Uri;
import com.atlassian.labs.remoteapps.plugin.util.uri.UriBuilder;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.elements.ResourceDescriptor;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.atlassian.plugin.webresource.UrlMode;
import com.atlassian.plugin.webresource.WebResourceManager;
import com.atlassian.plugin.webresource.WebResourceUrlProvider;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.atlassian.labs.remoteapps.plugin.util.EncodingUtils.*;
import static com.google.common.base.Preconditions.*;
import static com.google.common.collect.Lists.*;
import static com.google.common.collect.Maps.*;

@Component
public class IFrameRenderer
{
    private final TemplateRenderer templateRenderer;
    private final WebResourceManager webResourceManager;
    private final WebResourceUrlProvider webResourceUrlProvider;
    private final RemoteAppAccessorFactory remoteAppAccessorFactory;
    private final IFrameHost iframeHost;
    private final Plugin plugin;

    @Autowired
    public IFrameRenderer(TemplateRenderer templateRenderer,
            WebResourceManager webResourceManager,
            IFrameHost iframeHost,
            WebResourceUrlProvider webResourceUrlProvider,
            PluginRetrievalService pluginRetrievalService,
            RemoteAppAccessorFactory remoteAppAccessorFactory)
    {
        this.remoteAppAccessorFactory = remoteAppAccessorFactory;
        this.templateRenderer = checkNotNull(templateRenderer);
        this.webResourceManager = checkNotNull(webResourceManager);
        this.iframeHost = checkNotNull(iframeHost);
        this.webResourceUrlProvider = checkNotNull(webResourceUrlProvider);
        this.plugin = checkNotNull(pluginRetrievalService).getPlugin();
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
                throw new PermissionDeniedException(iframeContext.getPluginKey(), "Cannot render iframe for this page");
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
            ctx.put("contextPath", iframeHost.getContextPath());
            ctx.put("iframeHtml", render(iframeContext, extraPath, queryParams, remoteUser));
            ctx.put("decorator", pageInfo.getDecorator());

            templateRenderer.render("velocity/iframe-page" + pageInfo.getTemplateSuffix() + ".vm", ctx, writer);
        }
        catch (PermissionDeniedException ex)
        {
            templateRenderer.render(
                    "velocity/iframe-page-accessdenied" + pageInfo.getTemplateSuffix() + ".vm",
                    ImmutableMap.<String, Object>of(
                            "title", pageInfo.getTitle(),
                            "decorator", pageInfo.getDecorator()), writer);
        }
    }

    public String render(IFrameContext iframeContext, String extraPath, Map<String, String[]> queryParams, String remoteUser) throws IOException
    {
        webResourceManager.requireResourcesForContext("remoteapps-iframe");
        RemoteAppAccessor remoteAppAccessor = remoteAppAccessorFactory.get(
                iframeContext.getPluginKey());

        final String hostUrl = iframeHost.getUrl();
        final String iframeUrl = iframeContext.getIframePath() + ObjectUtils.toString(extraPath);

        Map<String,String[]> allParams = newHashMap(queryParams);
        allParams.put("user_id", new String[]{remoteUser});
        allParams.put("xdm_e", new String[]{hostUrl});
        allParams.put("xdm_c", new String[]{"channel-" + iframeContext.getNamespace()});
        allParams.put("xdm_p", new String[]{"1"});
        String signedUrl = remoteAppAccessor.signGetUrl(iframeUrl, allParams);

        // clear xdm params as they are added by easyxdm later
        signedUrl = new UriBuilder(Uri.parse(signedUrl))
                .removeQueryParameter("xdm_e")
                .removeQueryParameter("xdm_c")
                .removeQueryParameter("xdm_p")
                .toString();

        Map<String,Object> ctx = newHashMap(iframeContext.getIFrameParams().getAsMap());
        ctx.put("iframeSrcHtml", escapeQuotes(signedUrl));
        ctx.put("remoteapp", remoteAppAccessor);
        ctx.put("namespace", iframeContext.getNamespace());
        ctx.put("scriptUrls", getJavaScriptUrls());
        ctx.put("contextPath", iframeHost.getContextPath());
        ctx.put("userId", remoteUser == null ? "" : remoteUser);

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
