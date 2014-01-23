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
        InitializedBuilder context(Map<String, Object> additionalContext);
        /**
         * Appends a random String to the iframe's default namespace (usually just the module key). This is useful when
         * rendering multiple iframes for the same module on the same page (which can be the case for macros).
         */
        InitializedBuilder ensureUniqueNamespace();
        Map<String, Object> build();
    }
}
