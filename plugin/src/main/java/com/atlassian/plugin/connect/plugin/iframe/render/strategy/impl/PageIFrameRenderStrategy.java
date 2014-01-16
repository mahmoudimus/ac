package com.atlassian.plugin.connect.plugin.iframe.render.strategy.impl;

import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategy;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.atlassian.plugin.connect.plugin.iframe.context.ModuleContextParameters;
import com.atlassian.plugin.connect.plugin.iframe.render.context.IFrameRenderContextBuilderFactory;
import com.atlassian.plugin.connect.plugin.iframe.render.uri.IFrameUriBuilderFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Map;

/**
 *
 */
public class PageIFrameRenderStrategy implements IFrameRenderStrategy
{
    private final IFrameUriBuilderFactory iFrameUriBuilderFactory;
    private final IFrameRenderContextBuilderFactory iFrameRenderContextBuilderFactory;
    private final TemplateRenderer templateRenderer;
    private final String addOnKey;
    private final String moduleKey;
    private final String uriTemplate;
    private final String template;
    private final String decorator;
    private final String title;

    public PageIFrameRenderStrategy(IFrameUriBuilderFactory iFrameUriBuilderFactory,
            IFrameRenderContextBuilderFactory iFrameRenderContextBuilderFactory, TemplateRenderer templateRenderer,
            String addOnKey, String moduleKey, String uriTemplate, String template, String decorator, String title)
    {
        this.iFrameUriBuilderFactory = iFrameUriBuilderFactory;
        this.iFrameRenderContextBuilderFactory = iFrameRenderContextBuilderFactory;
        this.templateRenderer = templateRenderer;
        this.addOnKey = addOnKey;
        this.moduleKey = moduleKey;
        this.uriTemplate = uriTemplate;
        this.template = template;
        this.decorator = decorator;
        this.title = title;
    }

    @Override
    public void render(final ModuleContextParameters moduleContextParameters, final OutputStream outputStream) throws IOException
    {
        String signedUri = iFrameUriBuilderFactory.builder()
                .addOn(addOnKey)
                .module(moduleKey)
                .urlTemplate(uriTemplate)
                .context(moduleContextParameters)
                .signAndBuild();

        Map<String, Object> renderContext = iFrameRenderContextBuilderFactory.builder()
                .addOn(addOnKey)
                .module(moduleKey)
                .iframeUri(signedUri)
                .decorator(decorator)
                .title(title)
                .build();

        templateRenderer.render(template, renderContext, new OutputStreamWriter(outputStream));
    }
}
