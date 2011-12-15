package com.atlassian.labs.remoteapps.modules.webhook;

import com.atlassian.labs.remoteapps.modules.external.ClosableRemoteModule;
import com.atlassian.labs.remoteapps.modules.external.RemoteAppCreationContext;
import com.atlassian.labs.remoteapps.modules.external.RemoteModule;
import com.atlassian.labs.remoteapps.modules.external.RemoteModuleGenerator;
import com.atlassian.labs.remoteapps.product.ProductAccessor;
import com.atlassian.labs.remoteapps.webhook.WebHookPublisher;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.PluginParseException;
import org.dom4j.Element;

import java.util.Map;
import java.util.Set;

import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;

/**
 * Created by IntelliJ IDEA.
 * User: mrdon
 * Date: 15/12/11
 * Time: 9:33 PM
 * To change this template use File | Settings | File Templates.
 */
public class WebHookModuleGenerator implements RemoteModuleGenerator
{
    private final WebHookPublisher webHookPublisher;

    public WebHookModuleGenerator(WebHookPublisher webHookPublisher)
    {
        this.webHookPublisher = webHookPublisher;
    }

    @Override
    public String getType()
    {
        return "web-hook";
    }

    @Override
    public Set<String> getDynamicModuleTypeDependencies()
    {
        return emptySet();
    }

    @Override
    public Map<String, String> getI18nMessages(String pluginKey, Element element)
    {
        return emptyMap();
    }

    @Override
    public RemoteModule generate(final RemoteAppCreationContext ctx, Element element)
    {
        final String eventIdentifier = element.attributeValue("event");
        final String url = element.attributeValue("url");
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

    @Override
    public void validate(Element element) throws PluginParseException
    {
    }

    @Override
    public void convertDescriptor(Element descriptorElement, Element pluginDescriptorRoot)
    {
    }
}
