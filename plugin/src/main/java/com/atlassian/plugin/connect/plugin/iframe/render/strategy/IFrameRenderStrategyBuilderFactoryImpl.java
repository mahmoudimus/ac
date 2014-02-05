package com.atlassian.plugin.connect.plugin.iframe.render.strategy;

import com.atlassian.plugin.connect.plugin.iframe.render.context.IFrameRenderContextBuilderFactory;
import com.atlassian.plugin.connect.plugin.iframe.render.uri.IFrameUriBuilderFactory;
import com.atlassian.templaterenderer.TemplateRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 */
@Component
public class IFrameRenderStrategyBuilderFactoryImpl implements IFrameRenderStrategyBuilderFactory
{
    private final IFrameUriBuilderFactory iFrameUriBuilderFactory;
    private final IFrameRenderContextBuilderFactory iFrameRenderContextBuilderFactory;
    private final TemplateRenderer templateRenderer;

    @Autowired
    public IFrameRenderStrategyBuilderFactoryImpl(final IFrameUriBuilderFactory iFrameUriBuilderFactory,
            final IFrameRenderContextBuilderFactory iFrameRenderContextBuilderFactory,
            final TemplateRenderer templateRenderer)
    {
        this.iFrameUriBuilderFactory = iFrameUriBuilderFactory;
        this.iFrameRenderContextBuilderFactory = iFrameRenderContextBuilderFactory;
        this.templateRenderer = templateRenderer;
    }

    @Override
    public IFrameRenderStrategyBuilder builder()
    {
        return new IFrameRenderStrategyBuilderImpl(iFrameUriBuilderFactory, iFrameRenderContextBuilderFactory,
                templateRenderer);
    }
}
