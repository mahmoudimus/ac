package com.atlassian.labs.remoteapps.plugin.module.page.dialog;

import com.atlassian.labs.remoteapps.plugin.module.page.AbstractPageModuleGenerator;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;

/**
 * Generates plugin modules for a <dialog-page/> Remote Module
 */
@Component
public class DialogPageModuleGenerator extends AbstractPageModuleGenerator
{
    @Autowired
    public DialogPageModuleGenerator(PluginRetrievalService pluginRetrievalService)
    {
        super(pluginRetrievalService);
    }

    @Override
    public String getType()
    {
        return "dialog-page";
    }

    @Override
    public String getName()
    {
        return "Dialog Page";
    }

    @Override
    public String getDescription()
    {
        return "Loads a Remote App page (iframe-based) in an AUI Dialog";
    }
}
