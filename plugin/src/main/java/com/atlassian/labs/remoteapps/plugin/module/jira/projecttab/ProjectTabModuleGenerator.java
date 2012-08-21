package com.atlassian.labs.remoteapps.plugin.module.jira.projecttab;

import com.atlassian.labs.remoteapps.plugin.module.RemoteModuleGenerator;
import com.atlassian.labs.remoteapps.spi.schema.DocumentBasedSchema;
import com.atlassian.labs.remoteapps.spi.schema.Schema;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import org.dom4j.Element;

import java.net.URI;
import java.util.Map;

import static com.atlassian.labs.remoteapps.spi.util.Dom4jUtils.getRequiredUriAttribute;
import static java.util.Collections.emptyMap;

/**
 *
 */
public class ProjectTabModuleGenerator implements RemoteModuleGenerator
{
    private final Plugin plugin;

    public ProjectTabModuleGenerator(PluginRetrievalService pluginRetrievalService)
    {
        this.plugin = pluginRetrievalService.getPlugin();
    }

    @Override
    public String getType()
    {
        return "project-tab";
    }

    @Override
    public Schema getSchema()
    {
        return DocumentBasedSchema.builder("project-tab-page")
                .setPlugin(plugin)
                .setName(getName())
                .setElementName("project-tab")
                .setDescription(getDescription())
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
        Element copy = descriptorElement.createCopy("project-tab-page");
        pluginDescriptorRoot.add(copy);
    }

    @Override
    public Map<String, String> getI18nMessages(String pluginKey, Element element)
    {
        return emptyMap();
    }

    @Override
    public String getName()
    {
        return "Project Tab";
    }

    @Override
    public String getDescription()
    {
        return "A remote page decorated as its own JIRA project tab";
    }
}
