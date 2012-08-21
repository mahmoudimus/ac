package com.atlassian.labs.remoteapps.plugin.installer;

import com.atlassian.labs.remoteapps.plugin.ModuleGeneratorManager;
import com.atlassian.labs.remoteapps.plugin.module.RemoteModuleGenerator;
import org.dom4j.Element;

import java.net.URI;
import java.util.Properties;

/**
* Validates modules and stores i18n file
*/
class ValidateModuleHandler implements ModuleGeneratorManager.ModuleHandler
{
    private final URI registrationUrl;
    private final String username;
    private final Properties props;
    private final String pluginKey;

    public ValidateModuleHandler(URI registrationUrl, String username,
            Properties props,
            String pluginKey)
    {
        this.registrationUrl = registrationUrl;
        this.username = username;
        this.props = props;
        this.pluginKey = pluginKey;
    }

    @Override
    public void handle(Element element, RemoteModuleGenerator generator)
    {
        generator.validate(element, registrationUrl, username);
        props.putAll(generator.getI18nMessages(pluginKey, element));
    }
}
