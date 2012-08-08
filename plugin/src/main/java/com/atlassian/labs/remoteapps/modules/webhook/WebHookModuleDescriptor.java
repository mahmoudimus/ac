package com.atlassian.labs.remoteapps.modules.webhook;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.labs.remoteapps.event.RemoteAppStartedEvent;
import com.atlassian.labs.remoteapps.loader.StartableForPlugins;
import com.atlassian.labs.remoteapps.webhook.WebHookPublisher;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.descriptors.AbstractModuleDescriptor;
import com.atlassian.util.concurrent.NotNull;
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
    private final EventPublisher eventPublisher;
    private final StartableForPlugins startableForPlugins;

    public WebHookModuleDescriptor(
            WebHookPublisher webHookPublisher, EventPublisher eventPublisher,
            StartableForPlugins startableForPlugins)
    {
        this.webHookPublisher = webHookPublisher;
        this.eventPublisher = eventPublisher;
        this.startableForPlugins = startableForPlugins;
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

        // fixme: this is a workaround until we fully get rid of the started event or handle it differently
        if ("remote_app_started".equals(eventIdentifier))
        {
            startableForPlugins.register(getPluginKey(), new Runnable()
            {
                @Override
                public void run()
                {
                    eventPublisher.publish(new RemoteAppStartedEvent(getPluginKey()));
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
