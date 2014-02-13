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
        TemplatedBuilder pageTemplate();
        TemplatedBuilder genericBodyTemplate();
        TemplatedBuilder genericBodyTemplate(boolean inline);
        TemplatedBuilder dialogTemplate();
        TemplatedBuilder projectAdminTabTemplate();
        TemplatedBuilder workflowPostFunctionTemplate();
    }

    interface TemplatedBuilder
    {
        InitializedBuilder urlTemplate(String urlTemplate);
    }

    interface InitializedBuilder
    {
        InitializedBuilder condition(Condition condition);
        InitializedBuilder title(String title);
        InitializedBuilder dimensions(String width, String height);
        InitializedBuilder decorator(String decorator);
        InitializedBuilder additionalRenderContext(String key, Object object);
        InitializedBuilder additionalRenderContext(Map<String, Object> additionalRenderContext);
        InitializedBuilder ensureUniqueNamespace(boolean uniqueNamespace);
        InitializedBuilder dialog(boolean isDialog);
        IFrameRenderStrategy build();
    }
}
