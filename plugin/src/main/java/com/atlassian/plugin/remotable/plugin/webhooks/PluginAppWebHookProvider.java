package com.atlassian.plugin.remotable.plugin.webhooks;

import com.atlassian.plugin.remotable.spi.webhook.EventMatcher;
import com.atlassian.plugin.remotable.spi.webhook.WebHookProvider;
import com.atlassian.plugin.remotable.spi.webhook.WebHookRegistrar;

/**
 * Web hooks for the plugin itself
 */
public final class PluginAppWebHookProvider implements WebHookProvider
{
    @Override
    public void provide(WebHookRegistrar publish)
    {
        // these gets fired manually via the {@link WebHookModuleDescriptor}
        publish.webhook("plugin_enabled").whenFired(Object.class).matchedBy(EventMatcher.ALWAYS_FALSE);
    }
}
