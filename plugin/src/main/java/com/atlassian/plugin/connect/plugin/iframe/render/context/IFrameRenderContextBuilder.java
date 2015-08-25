package com.atlassian.plugin.connect.plugin.iframe.render.context;

import java.util.Map;

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
        InitializedBuilder dialog(boolean isDialog);
        InitializedBuilder simpleDialog(boolean isSimpleDialog);
        <T extends Map<String, String>> InitializedBuilder productContext(T productContext);
        InitializedBuilder decorator(String decorator);
        InitializedBuilder title(String title);
        InitializedBuilder resizeToParent(boolean resizeToParent);
        InitializedBuilder context(String key, Object value);
        InitializedBuilder context(Map<String, Object> additionalContext);

        /**
         * @param host - origin adress of the add-on server. Origin is required to setup proper XDM connection.
         * Is needed only if iframe points to the redirection servlet because in that case it can not be obtained from url.
         */
        InitializedBuilder origin(String host);
        Map<String, Object> build();
    }
}
