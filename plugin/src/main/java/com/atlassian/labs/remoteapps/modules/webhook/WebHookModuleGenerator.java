package com.atlassian.labs.remoteapps.modules.webhook;

import com.atlassian.labs.remoteapps.modules.external.*;
import com.atlassian.labs.remoteapps.webhook.WebHookPublisher;
import com.atlassian.labs.remoteapps.webhook.WebHookRegistrationManager;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.PluginParseException;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.Map;
import java.util.Set;

import static com.atlassian.labs.remoteapps.util.Dom4jUtils.getRequiredUriAttribute;
import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;

/**
 * Registers webhooks
 */
@Component
public class WebHookModuleGenerator implements WaitableRemoteModuleGenerator
{
    private final WebHookPublisher webHookPublisher;
    private final WebHookSchema webHookSchema;
    private final WebHookRegistrationManager webHookRegistrationManager;

    @Autowired
    public WebHookModuleGenerator(WebHookPublisher webHookPublisher, WebHookSchema webHookSchema,
            WebHookRegistrationManager webHookRegistrationManager)
    {
        this.webHookPublisher = webHookPublisher;
        this.webHookSchema = webHookSchema;
        this.webHookRegistrationManager = webHookRegistrationManager;
    }

    @Override
    public String getType()
    {
        return "web-hook";
    }

    @Override
    public String getName()
    {
        return "Web Hook";
    }

    @Override
    public String getDescription()
    {
        return "Registration for a web hook callback from an internal event";
    }

    @Override
    public Schema getSchema()
    {
        return webHookSchema;
    }

    @Override
    public Map<String, String> getI18nMessages(String pluginKey, Element element)
    {
        return emptyMap();
    }

    @Override
    public RemoteModule generate(final RemoteAppCreationContext ctx, Element element)
    {
        final String eventIdentifier = getWebHookId(element);
        final String url = getRequiredUriAttribute(element, "url").toString();
        webHookPublisher.register(ctx.getApplicationType(), eventIdentifier, url);
        return new ClosableRemoteModule()
        {
            @Override
            public Set<ModuleDescriptor> getModuleDescriptors()
            {
                return emptySet();
            }

            @Override
            public void close()
            {
                webHookPublisher.unregister(ctx.getApplicationType(), eventIdentifier, url);
            }
        };
    }

    private String getWebHookId(Element element)
    {
        return element.attributeValue("event");
    }

    @Override
    public void validate(Element element, URI registrationUrl, String username) throws PluginParseException
    {
        getRequiredUriAttribute(element, "url");
    }

    @Override
    public void generatePluginDescriptor(Element descriptorElement, Element pluginDescriptorRoot)
    {
    }

    @Override
    public void waitToLoad(Element element)
    {
        webHookRegistrationManager.waitForId(getWebHookId(element));
    }
}
