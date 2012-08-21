package com.atlassian.labs.remoteapps.plugin.module.page;

import com.atlassian.labs.remoteapps.spi.schema.DocumentBasedSchema;
import com.atlassian.labs.remoteapps.spi.schema.Schema;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import org.dom4j.Element;

import java.util.Map;

import static java.util.Collections.emptyMap;

/**
 * Module type for user profile pages, generating a web item and servlet with iframe
 */
public class ProfilePageModuleGenerator extends AbstractPageModuleGenerator
{
    public ProfilePageModuleGenerator(PluginRetrievalService pluginRetrievalService)
    {
        super(pluginRetrievalService);
    }

    @Override
    public String getType()
    {
        return "profile-page";
    }

    @Override
    public String getDescription()
    {
        return "A user profile page decorated as normal page in the user profile area";
    }

    @Override
    public String getName()
    {
        return "User Profile Page";
    }

    @Override
    public Schema getSchema()
    {
        return DocumentBasedSchema.builder("profile-page")
                .setPlugin(getPlugin())
                .setName(getName())
                .setDescription(getDescription())
                .build();
    }

    @Override
    public Map<String, String> getI18nMessages(String pluginKey, Element element)
    {
        return emptyMap();
    }

}
