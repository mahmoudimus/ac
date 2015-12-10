package com.atlassian.plugin.connect.plugin.web.iframe;

import com.atlassian.plugin.connect.api.web.iframe.IFrameRenderStrategyBuilder;
import com.atlassian.plugin.connect.api.web.iframe.IFrameRenderStrategyBuilderFactory;
import com.atlassian.plugin.connect.plugin.web.condition.ConnectConditionFactory;
import com.atlassian.plugin.connect.api.web.iframe.IFrameUriBuilderFactory;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsDevService;
import com.atlassian.templaterenderer.TemplateRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 */
@Component
@ExportAsDevService
public class IFrameRenderStrategyBuilderFactoryImpl implements IFrameRenderStrategyBuilderFactory
{
    private final IFrameUriBuilderFactory iFrameUriBuilderFactory;
    private final IFrameRenderContextBuilderFactory iFrameRenderContextBuilderFactory;
    private final TemplateRenderer templateRenderer;
    private final ConnectConditionFactory connectConditionFactory;

    @Autowired
    public IFrameRenderStrategyBuilderFactoryImpl(IFrameUriBuilderFactory iFrameUriBuilderFactory,
            IFrameRenderContextBuilderFactory iFrameRenderContextBuilderFactory, TemplateRenderer templateRenderer,
            ConnectConditionFactory connectConditionFactory)
    {
        this.iFrameUriBuilderFactory = iFrameUriBuilderFactory;
        this.iFrameRenderContextBuilderFactory = iFrameRenderContextBuilderFactory;
        this.templateRenderer = templateRenderer;
        this.connectConditionFactory = connectConditionFactory;
    }

    @Override
    public IFrameRenderStrategyBuilder builder()
    {
        return new IFrameRenderStrategyBuilderImpl(iFrameUriBuilderFactory, iFrameRenderContextBuilderFactory,
                templateRenderer, connectConditionFactory);
    }
}
