package com.atlassian.plugin.connect.plugin.iframe.render.context;

import java.util.Map;

/**
 *
 */
public interface IFrameRenderContextBuilder
{
    AddOnContextBuilder addOn(String key);

    interface AddOnContextBuilder
    {
        NamespacedContextBuilder namespace(String namespace);
    }

    interface NamespacedContextBuilder
    {
        InitializedBuilder iframeUri(String uri);
    }

    interface InitializedBuilder
    {
        InitializedBuilder dialog(String dialogId);
        InitializedBuilder simpleDialog(String simpleDialogId);
        InitializedBuilder productContext(Map<String, Object> productContext);
        InitializedBuilder decorator(String decorator);
        InitializedBuilder title(String title);
        InitializedBuilder context(String key, Object value);
        InitializedBuilder context(Map<String, Object> additionalContext);
        Map<String, Object> build();
    }
}
