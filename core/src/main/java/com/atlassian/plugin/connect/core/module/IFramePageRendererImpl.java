package com.atlassian.plugin.connect.core.module;

import com.atlassian.plugin.connect.spi.iframe.page.IFramePageRenderer;
import com.atlassian.plugin.connect.spi.module.page.PageInfo;
import com.atlassian.plugin.connect.spi.PermissionDeniedException;
import com.atlassian.plugin.connect.spi.module.IFrameContext;
import com.atlassian.plugin.connect.spi.module.IFrameRenderer;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.google.common.collect.ImmutableMap;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.atlassian.plugin.connect.core.iframe.servlet.ConnectIFrameServlet.CLASSIFIER;
import static com.atlassian.plugin.connect.core.iframe.servlet.ConnectIFrameServlet.RAW_CLASSIFIER;
import static com.google.common.collect.Maps.newHashMap;

@Component
public class IFramePageRendererImpl implements IFramePageRenderer
{
    public static final String ATL_GENERAL = "atl.general";
    private final TemplateRenderer templateRenderer;
    private final IFrameRenderer iframeRenderer;
    private final HostApplicationInfo hostApplicationInfo;

    @Inject
    public IFramePageRendererImpl(TemplateRenderer templateRenderer, IFrameRenderer iframeRenderer, HostApplicationInfo hostApplicationInfo)
    {
        this.templateRenderer = templateRenderer;
        this.iframeRenderer = iframeRenderer;
        this.hostApplicationInfo = hostApplicationInfo;
    }

    @Override
    public void renderPage(IFrameContext iframeContext, PageInfo pageInfo, String extraPath, Map<String, String[]> queryParams, String remoteUser, Map<String, Object> productContext, Writer writer) throws IOException
    {
        try
        {
            if (!pageInfo.getCondition().shouldDisplay(Collections.<String, Object>emptyMap()))
            {
                throw new PermissionDeniedException(iframeContext.getPluginKey(), "Cannot render iframe for this page");
            }

            Map<String, Object> ctx = newHashMap(iframeContext.getIFrameParams().getAsMap());

            if (RAW_CLASSIFIER.equals(queryParams.get(CLASSIFIER)))
            {
                iframeContext.getIFrameParams().setParam("width", "100%");
                iframeContext.getIFrameParams().setParam("height", "100%");
            }
            else
            {
                if (queryParams.get("width") != null)
                {
                    iframeContext.getIFrameParams().setParam("width", queryParams.get("width")[0]);
                }
                if (queryParams.get("height") != null)
                {
                    iframeContext.getIFrameParams().setParam("height", queryParams.get("height")[0]);
                }
            }

            ctx.put("queryParams", contextQueryParameters(queryParams));
            ctx.put("title", pageInfo.getTitle());
            ctx.put("contextPath", hostApplicationInfo.getContextPath());
            ctx.put("iframeHtml", iframeRenderer.render(iframeContext, extraPath, queryParams, remoteUser, productContext));
            ctx.put("decorator", pageInfo.getDecorator());

            for (Map.Entry<String, String> metaTag : pageInfo.getMetaTagsContent().entrySet())
            {
                ctx.put(metaTag.getKey(), metaTag.getValue());
            }

            templateRenderer.render("velocity/deprecated/iframe-page" + pageInfo.getTemplateSuffix() + ".vm", ctx, writer);
        }
        catch (PermissionDeniedException ex)
        {
            templateRenderer.render(
                    "velocity/deprecated/iframe-page-accessdenied" + pageInfo.getTemplateSuffix() + ".vm",
                    ImmutableMap.<String, Object>of(
                            "title", pageInfo.getTitle(),
                            "decorator", ATL_GENERAL), writer);
        }
    }

    private Map<String, List<String>> contextQueryParameters(final Map<String, String[]> queryParams)
    {
        final Map<String, List<String>> ctxQueryParams = newHashMap();
        for (Map.Entry<String, String[]> param : queryParams.entrySet())
        {
            ctxQueryParams.put(param.getKey(), Arrays.asList(param.getValue()));
        }
        return ctxQueryParams;
    }
}
