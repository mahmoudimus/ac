package com.atlassian.plugin.connect.plugin.iframe.render.strategy;

import com.atlassian.jira.issue.fields.rest.json.beans.JiraBaseUrls;
import com.atlassian.plugin.connect.api.iframe.render.strategy.IFrameRenderStrategyBuilder;
import com.atlassian.plugin.connect.api.iframe.render.strategy.IFrameRenderStrategyBuilderFactory;
import com.atlassian.plugin.connect.api.module.webfragment.UrlVariableSubstitutor;
import com.atlassian.plugin.connect.plugin.capabilities.condition.ConnectConditionFactory;
import com.atlassian.plugin.connect.plugin.iframe.render.context.IFrameRenderContextBuilderFactory;
import com.atlassian.plugin.connect.api.iframe.render.uri.IFrameUriBuilderFactory;
import com.atlassian.plugin.connect.spi.RemotablePluginAccessorFactory;
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
    private final ConnectConditionFactory connectConditionFactory;
    private final UrlVariableSubstitutor urlVariableSubstitutor;
    private final RemotablePluginAccessorFactory pluginAccessorFactory;

    @Autowired
    public IFrameRenderStrategyBuilderFactoryImpl(IFrameUriBuilderFactory iFrameUriBuilderFactory,
            IFrameRenderContextBuilderFactory iFrameRenderContextBuilderFactory, TemplateRenderer templateRenderer,
            ConnectConditionFactory connectConditionFactory,
            JiraBaseUrls jiraBaseUrls,
            UrlVariableSubstitutor urlVariableSubstitutor,
            RemotablePluginAccessorFactory pluginAccessorFactory)
    {
        this.iFrameUriBuilderFactory = iFrameUriBuilderFactory;
        this.iFrameRenderContextBuilderFactory = iFrameRenderContextBuilderFactory;
        this.templateRenderer = templateRenderer;
        this.connectConditionFactory = connectConditionFactory;
        this.urlVariableSubstitutor = urlVariableSubstitutor;
        this.pluginAccessorFactory = pluginAccessorFactory;
    }

    @Override
    public IFrameRenderStrategyBuilder builder()
    {
        return new IFrameRenderStrategyBuilderImpl(iFrameUriBuilderFactory, iFrameRenderContextBuilderFactory,
                templateRenderer, connectConditionFactory, urlVariableSubstitutor, pluginAccessorFactory);
    }
}
