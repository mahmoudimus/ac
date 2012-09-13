package com.atlassian.labs.remoteapps.plugin.module.page;

import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

import static java.util.Collections.emptyMap;

/**
 * Module type for general pages, generating a web item and servlet with iframe
 */
@Component
public class GeneralPageModuleGenerator extends AbstractPageModuleGenerator
{
    @Autowired
    public GeneralPageModuleGenerator(PluginRetrievalService pluginRetrievalService
    )
    {
        super(pluginRetrievalService);
    }

    @Override
    public String getType()
    {
        return "general-page";
    }

    @Override
    public String getName()
    {
        return "General Page";
    }

    @Override
    public String getDescription()
    {
        return "A non-admin general page decorated by the application, with a link in a globally-accessible place";
    }
}
