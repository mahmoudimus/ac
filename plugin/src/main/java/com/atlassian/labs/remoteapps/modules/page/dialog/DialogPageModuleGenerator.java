package com.atlassian.labs.remoteapps.modules.page.dialog;

import com.atlassian.labs.remoteapps.RemoteAppAccessorFactory;
import com.atlassian.labs.remoteapps.modules.DefaultWebItemContext;
import com.atlassian.labs.remoteapps.modules.IFrameRenderer;
import com.atlassian.labs.remoteapps.modules.page.AbstractPageModuleGenerator;
import com.atlassian.labs.remoteapps.product.ProductAccessor;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.atlassian.plugin.servlet.ServletModuleManager;
import com.atlassian.sal.api.user.UserManager;
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
    public Map<String, String> getI18nMessages(String pluginKey, Element element)
    {
        return Collections.emptyMap();
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
