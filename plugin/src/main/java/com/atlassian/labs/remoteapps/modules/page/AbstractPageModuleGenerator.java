package com.atlassian.labs.remoteapps.modules.page;

import com.atlassian.labs.remoteapps.modules.external.*;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.URI;

import static com.atlassian.labs.remoteapps.util.Dom4jUtils.getRequiredUriAttribute;

/**
 * Abstract module type for canvas pages, generating a web item and servlet with iframe
 */
public abstract class AbstractPageModuleGenerator implements RemoteModuleGenerator
{
    private final Plugin plugin;

    @Autowired
    public AbstractPageModuleGenerator(PluginRetrievalService pluginRetrievalService)
    {
        this.plugin = pluginRetrievalService.getPlugin();
    }

    @Override
    public Schema getSchema()
    {
        return DocumentBasedSchema.builder("page")
            .setPlugin(getPlugin())
            .setTitle(getName())
            .setDescription(getDescription())
            .build();
    }

    @Override
    public void validate(Element element, URI registrationUrl, String username) throws PluginParseException
    {
        getRequiredUriAttribute(element, "url");
    }

    @Override
    public RemoteModule generate(RemoteAppCreationContext ctx, Element e)
    {
        return RemoteModule.NO_OP;
    }

    @Override
    public void generatePluginDescriptor(Element descriptorElement, Element pluginDescriptorRoot)
    {
        Element copy = descriptorElement.createCopy();
        pluginDescriptorRoot.add(copy);
    }

    protected Plugin getPlugin()
    {
        return plugin;
    }
}
