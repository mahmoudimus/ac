package com.atlassian.plugin.connect.plugin.iframe.render.strategy;

import com.atlassian.plugin.web.Condition;

import java.util.Map;

/**
 *
 */
public interface IFrameRenderStrategyBuilder
{
    AddOnUriBuilder addOn(String key);

    interface AddOnUriBuilder
    {
        ModuleUriBuilder module(String key);
    }

    interface ModuleUriBuilder
    {
        TemplatedBuilder genericPageTemplate();
        TemplatedBuilder dialogTemplate();
        TemplatedBuilder projectAdminTabTemplate();
    }

    interface TemplatedBuilder
    {
        InitializedBuilder urlTemplate(String urlTemplate);
    }

    interface InitializedBuilder
    {
        InitializedBuilder condition(Condition condition);
        InitializedBuilder title(String title);
        InitializedBuilder decorator(String decorator);
        InitializedBuilder additionalRenderContext(String key, Object object);
        InitializedBuilder additionalRenderContext(Map<String, Object> additionalRenderContext);
        IFrameRenderStrategy build();
    }
}
