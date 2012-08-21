package com.atlassian.labs.remoteapps.plugin.module.page;

import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

import static java.util.Collections.emptyMap;

/**
 * Module type for admin pages, generating a web item and servlet with iframe
 */
@Component
public class AdminPageModuleGenerator extends AbstractPageModuleGenerator
{

    @Autowired
    public AdminPageModuleGenerator(PluginRetrievalService pluginRetrievalService
    )
    {
        super(pluginRetrievalService);
    }

    @Override
    public String getType()
    {
        return "admin-page";
    }

    @Override
    public String getName()
    {
        return "Administration Page";
    }

    @Override
    public String getDescription()
    {
        return "An admin page decorated in the admin section, with a link in the admin menu";
    }

    @Override
    public Map<String, String> getI18nMessages(String pluginKey, Element element)
    {
        return emptyMap();
    }
}
