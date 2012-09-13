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

    public ValidateModuleHandler(URI registrationUrl, String username)
    {
        this.registrationUrl = registrationUrl;
        this.username = username;
    }

    @Override
    public void handle(Element element, RemoteModuleGenerator generator)
    {
        generator.validate(element, registrationUrl, username);
    }
}
