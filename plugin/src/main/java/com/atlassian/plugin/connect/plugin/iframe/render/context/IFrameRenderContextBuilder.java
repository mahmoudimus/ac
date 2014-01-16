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
        ModuleContextBuilder module(String key);
    }

    interface ModuleContextBuilder
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
        Map<String, Object> build();
    }
}
