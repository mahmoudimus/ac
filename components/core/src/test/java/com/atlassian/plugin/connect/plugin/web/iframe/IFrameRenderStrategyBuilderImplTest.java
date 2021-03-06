package com.atlassian.plugin.connect.plugin.web.iframe;

import com.atlassian.plugin.connect.api.web.iframe.ConnectUriFactory;
import com.atlassian.plugin.connect.plugin.web.condition.ConnectConditionFactory;
import com.atlassian.templaterenderer.TemplateRenderer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;

@RunWith(MockitoJUnitRunner.class)
public class IFrameRenderStrategyBuilderImplTest {
    private
    @Mock
    ConnectUriFactory connectUriFactory;
    private
    @Mock
    IFrameRenderContextBuilderFactory iFrameRenderContextBuilderFactory;
    private
    @Mock
    TemplateRenderer templateRenderer;
    private
    @Mock
    ConnectConditionFactory connectConditionFactory;

    @Test
    public void nullTitleDoesNotProduceNpe() throws IOException {
        new IFrameRenderStrategyBuilderImpl(connectUriFactory, iFrameRenderContextBuilderFactory, templateRenderer, connectConditionFactory)
                .title(null)
                .build()
                .renderAccessDenied(null);
    }
}
