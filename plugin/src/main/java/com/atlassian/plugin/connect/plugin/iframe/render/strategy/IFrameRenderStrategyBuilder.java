package com.atlassian.plugin.connect.plugin.iframe.render.strategy;

import com.atlassian.plugin.web.Condition;

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
        TemplatedBuilder template(String templatePath);
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
        IFrameRenderStrategy build();
    }
}
