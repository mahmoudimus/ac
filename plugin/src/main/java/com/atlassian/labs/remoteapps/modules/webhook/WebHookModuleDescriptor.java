package com.atlassian.labs.remoteapps.modules.webhook;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.labs.remoteapps.loader.StartableForPlugins;
import com.atlassian.labs.remoteapps.webhook.MapEventSerializer;
import com.atlassian.labs.remoteapps.webhook.WebHookPublisher;
import com.atlassian.labs.remoteapps.webhook.external.EventMatcher;
import com.atlassian.oauth.consumer.ConsumerService;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.descriptors.AbstractModuleDescriptor;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.util.concurrent.NotNull;
import com.google.common.collect.ImmutableMap;
import org.dom4j.Element;

import static com.atlassian.labs.remoteapps.util.Dom4jUtils.getOptionalAttribute;
import static com.atlassian.labs.remoteapps.util.Dom4jUtils.getRequiredUriAttribute;

/**
 * Registers web hooks
 */
public class WebHookModuleDescriptor extends AbstractModuleDescriptor<Void>
{
    private String eventIdentifier;
    private String url;
    private final WebHookPublisher webHookPublisher;
    private final StartableForPlugins startableForPlugins;
    private final ApplicationProperties applicationProperties;
    private final ConsumerService consumerService;

    public WebHookModuleDescriptor(WebHookPublisher webHookPublisher,
                                   StartableForPlugins startableForPlugins,
                                   ApplicationProperties applicationProperties,
                                   ConsumerService consumerService
    )
    {
        this.webHookPublisher = webHookPublisher;
        this.startableForPlugins = startableForPlugins;
        this.applicationProperties = applicationProperties;
        this.consumerService = consumerService;
    }

    @Override
    public Void getModule()
    {
        return null;
    }

    @Override
    public void init(@NotNull Plugin plugin, @NotNull Element element) throws PluginParseException
    {
        super.init(plugin, element);
        eventIdentifier = getOptionalAttribute(element, "event", getKey());
        url = getRequiredUriAttribute(element, "url").toString();


    }

    @Override
    public void enabled()
    {
        super.enabled();
        webHookPublisher.register(getPluginKey(), eventIdentifier, url);

        if ("remote_app_started".equals(eventIdentifier) || "plugin_started".equals(eventIdentifier))
        {
            startableForPlugins.register(getPluginKey(), new Runnable()
            {
                @Override
                public void run()
                {
                    final String baseUrl = WebHookModuleDescriptor.this.applicationProperties.getBaseUrl();
                    webHookPublisher.publish(eventIdentifier, EventMatcher.ALWAYS_TRUE,
                     new MapEventSerializer(null, ImmutableMap.<String, Object>of(
                             "key", getPluginKey(),
                             "serverKey", WebHookModuleDescriptor.this.consumerService.getConsumer().getKey(),
                             "baseurl", (baseUrl != null ? baseUrl : ""),
                             "baseUrl", (baseUrl != null ? baseUrl : ""))));
                }
            });
        }
    }

    @Override
    public void disabled()
    {
        super.disabled();
        webHookPublisher.unregister(getPluginKey(), eventIdentifier, url);
    }
}
