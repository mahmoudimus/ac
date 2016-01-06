package com.atlassian.plugin.connect.plugin.web.iframe;

import java.util.Map;

public interface IFrameRenderContextBuilder
{
    AddonContextBuilder addon(String key);

    interface AddonContextBuilder
    {
        NamespacedContextBuilder namespace(String namespace);
    }

    interface NamespacedContextBuilder
    {
        InitializedBuilder iframeUri(String uri);
    }

    interface InitializedBuilder
    {
        InitializedBuilder dialog(boolean isDialog);
        InitializedBuilder simpleDialog(boolean isSimpleDialog);
        <T extends Map<String, String>> InitializedBuilder productContext(T productContext);
        InitializedBuilder decorator(String decorator);
        InitializedBuilder title(String title);
        InitializedBuilder resizeToParent(boolean resizeToParent);
        InitializedBuilder context(String key, Object value);
        InitializedBuilder context(Map<String, Object> additionalContext);
        Map<String, Object> build();
    }
}
