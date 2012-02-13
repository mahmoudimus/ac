package com.atlassian.labs.remoteapps.modules;

import com.atlassian.labs.remoteapps.modules.page.IFrameContext;
import com.atlassian.plugin.webresource.WebResourceManager;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.templaterenderer.TemplateRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.util.Collections;
import java.util.Map;

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

    @Autowired
    public IFrameRenderer(TemplateRenderer templateRenderer,
                          WebResourceManager webResourceManager,
                          ApplicationProperties applicationProperties
    )
    {
        this.templateRenderer = templateRenderer;
        this.webResourceManager = webResourceManager;
        this.applicationProperties = applicationProperties;
    }

    public String render(IFrameContext iframeContext, String remoteUser) throws IOException
    {
        return render(iframeContext, "", Collections.<String, String[]>emptyMap(), remoteUser);
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

        Map<String,String[]> allParams = newHashMap(queryParams);
        allParams.put("xdm_e", new String[]{host});
        allParams.put("xdm_c", new String[]{"channel-" + iframeContext.getNamespace()});
        allParams.put("xdm_p", new String[]{"1"});
        String signedUrl = iframeContext.getLinkOps().signGetUrl(remoteUser, iframeContext.getIframePath(), allParams);

        // clear xdm params as they are added by easyxdm later
        signedUrl = UriBuilder.fromUri(signedUrl)
                    .replaceQueryParam("xdm_e")
                    .replaceQueryParam("xdm_c")
                    .replaceQueryParam("xdm_p").build().toString();


        Map<String,Object> ctx = newHashMap(iframeContext.getIFrameParams().getAsMap());
        ctx.put("iframeSrcHtml", signedUrl);
        ctx.put("extraPath", extraPath != null ? extraPath : "");
        ctx.put("remoteapp", iframeContext.getLinkOps().get());
        ctx.put("namespace", iframeContext.getNamespace());

        StringWriter output = new StringWriter();
        templateRenderer.render("velocity/iframe-body.vm", ctx, output);
        return output.toString();
    }
}
