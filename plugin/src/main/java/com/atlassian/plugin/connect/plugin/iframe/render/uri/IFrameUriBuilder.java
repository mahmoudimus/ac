package com.atlassian.plugin.connect.plugin.iframe.render.uri;

import com.atlassian.plugin.connect.plugin.iframe.context.ModuleContextParameters;

/**
 * Builds URIs to be used as the src attribute for iframes targeting Connect apps.
 */
public interface IFrameUriBuilder
{
    AddOnUriBuilder addOn(String key);

    interface AddOnUriBuilder
    {
        NamespacedUriBuilder namespace(String namespace);
    }

    interface NamespacedUriBuilder
    {
        TemplatedBuilder urlTemplate(String uri);
    }

    interface TemplatedBuilder
    {
        InitializedBuilder context(ModuleContextParameters context);
    }

    interface InitializedBuilder
    {
        InitializedBuilder param(String key, String value);
        String signAndBuild();
        String buildUnsigned();
    }
}
