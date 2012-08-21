package com.atlassian.labs.remoteapps.plugin.webhook;

import com.atlassian.labs.remoteapps.spi.webhook.EventMatcher;
import com.atlassian.labs.remoteapps.spi.webhook.WebHookProvider;
import com.atlassian.labs.remoteapps.spi.webhook.WebHookRegistrar;
import org.springframework.stereotype.Component;

/**
 * Web hooks for the plugin itself
 */
@Component
public class PluginAppWebHookProvider implements WebHookProvider
{
    @Override
    public void provide(WebHookRegistrar publish)
    {
        // these gets fired manually via the {@link WebHookModuleDescriptor}
        publish.webhook("plugin_enabled").whenFired(Object.class)
                .matchedBy(EventMatcher.ALWAYS_FALSE);

        // deprecated but if you remove, fix WebHookModuleDescriptor
        publish.webhook("remote_app_started").whenFired(Object.class)
                .matchedBy(EventMatcher.ALWAYS_FALSE);
    }
}
