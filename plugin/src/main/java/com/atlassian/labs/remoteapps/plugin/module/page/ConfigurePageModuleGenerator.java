package com.atlassian.labs.remoteapps.plugin.module.page;

import com.atlassian.labs.remoteapps.spi.schema.DocumentBasedSchema;
import com.atlassian.labs.remoteapps.spi.schema.Schema;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Module type for configuration pages in the admin area, generating a web item and servlet with
 * iframe but also add a configuration URL to the plugin descriptor
 */
@Component
public class ConfigurePageModuleGenerator extends AdminPageModuleGenerator
{
    @Autowired
    public ConfigurePageModuleGenerator(PluginRetrievalService pluginRetrievalService)
    {
        super(pluginRetrievalService);
    }

    @Override
    public String getType()
    {
        return "configure-page";
    }

    @Override
    public String getName()
    {
        return "Configure Page";
    }

    @Override
    public Schema getSchema()
    {
        return DocumentBasedSchema.builder("configure-page")
                .setPlugin(getPlugin())
                .setName(getName())
                .setDescription(getDescription())
                .setMaxOccurs("1")
                .build();
    }

    @Override
    public String getDescription()
    {
        return "The configuration page for the app, decorated in the admin section, with a link in" +
                "the admin menu and a configure link in the Plugin Manager";
    }

    @Override
    public void generatePluginDescriptor(Element descriptorElement, Element pluginDescriptorRoot)
    {
        super.generatePluginDescriptor(descriptorElement, pluginDescriptorRoot);
        String appKey = pluginDescriptorRoot.attributeValue("key");
        String pageKey = descriptorElement.attributeValue("key");
        pluginDescriptorRoot.element("plugin-info").addElement("param")
                .addAttribute("name", "configure.url")
                .addText("/plugins/servlet" + RemotePageDescriptorCreator.createLocalUrl(appKey,
                        pageKey));
    }
}
