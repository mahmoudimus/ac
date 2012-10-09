package com.atlassian.labs.remoteapps.plugin.module.webhook;

import com.atlassian.labs.remoteapps.plugin.module.RemoteModuleGenerator;
import com.atlassian.labs.remoteapps.plugin.webhook.WebHookIdsAccessor;
import com.atlassian.labs.remoteapps.spi.schema.DocumentBasedSchema;
import com.atlassian.labs.remoteapps.spi.schema.Schema;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URI;

import static com.atlassian.labs.remoteapps.spi.util.Dom4jUtils.*;
import static com.google.common.base.Preconditions.*;

/**
 * <p>Generates the <code>webhook</code> module for local plugin descriptor from the <code>web-hook</code> descriptor
 * used in remote apps descriptors.
 * <p>Also defines the schema for validating such <code>web-hook</code> module of the remote app descriptor. It will
 * dynamically list all web hook events available on the app.
 */
@Component
public final class WebHookModuleGenerator implements RemoteModuleGenerator
{
    private final WebHookIdsAccessor webHookIdsAccessor;
    private final Plugin plugin;

    @Autowired
    public WebHookModuleGenerator(WebHookIdsAccessor webHookIdsAccessor, PluginRetrievalService pluginRetrievalService)
    {
        this.webHookIdsAccessor = checkNotNull(webHookIdsAccessor);
        this.plugin = checkNotNull(pluginRetrievalService).getPlugin();
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
        return DocumentBasedSchema.builder("webhook")
                .setPlugin(plugin)
                .setName(getName())
                .setElementName("web-hook")
                .setDescription(getDescription())
                .setTransformer(new WebHookSchemaFactory(webHookIdsAccessor))
                .build();
    }

    @Override
    public void validate(Element element, URI registrationUrl, String username) throws PluginParseException
    {
        getRequiredUriAttribute(element, "url");
    }

    @Override
    public void generatePluginDescriptor(Element descriptorElement, Element pluginDescriptorRoot)
    {
        final Element copy = descriptorElement.createCopy("webhook")
                .addAttribute("key", "wh-" + getWebHookId(descriptorElement))
                .addAttribute("url", getWebHookUrl(descriptorElement, pluginDescriptorRoot));

        pluginDescriptorRoot.add(copy);
    }

    private String getWebHookId(Element element)
    {
        return element.attributeValue("event");
    }

    private String getWebHookUrl(Element descriptorElement, Element pluginDescriptorRoot)
    {
        return getDisplayUrl(pluginDescriptorRoot) + descriptorElement.attributeValue("url");
    }

    private String getDisplayUrl(Element root)
    {
        return root.element("remote-plugin-container").attributeValue("display-url");
    }
}
