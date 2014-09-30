package com.atlassian.plugin.connect.plugin.iframe.render.strategy;

import com.atlassian.plugin.connect.plugin.capabilities.condition.ConnectConditionFactory;
import com.atlassian.plugin.connect.plugin.iframe.render.context.IFrameRenderContextBuilderFactory;
import com.atlassian.plugin.connect.plugin.iframe.render.uri.IFrameUriBuilderFactory;
import com.atlassian.templaterenderer.TemplateRenderer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;

@RunWith(MockitoJUnitRunner.class)
public class IFrameRenderStrategyBuilderImplTest
{
    private @Mock IFrameUriBuilderFactory iFrameUriBuilderFactory;
    private @Mock IFrameRenderContextBuilderFactory iFrameRenderContextBuilderFactory;
    private @Mock TemplateRenderer templateRenderer;
    private @Mock ConnectConditionFactory connectConditionFactory;

    @Test
    public void nullTitleDoesNotProduceNpe() throws IOException
    {
        new IFrameRenderStrategyBuilderImpl(iFrameUriBuilderFactory, iFrameRenderContextBuilderFactory, templateRenderer, connectConditionFactory)
                .title(null)
                .build()
                .renderAccessDenied(null);
    }
}
