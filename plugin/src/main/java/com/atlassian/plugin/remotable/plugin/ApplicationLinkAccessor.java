package com.atlassian.plugin.remotable.plugin;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.ApplicationLinkService;
import com.atlassian.plugin.remotable.spi.applinks.RemotePluginContainerApplicationType;
import com.atlassian.plugin.remotable.plugin.module.applinks.RemotePluginContainerModuleDescriptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Accesses an application link
 */
@Component
public class ApplicationLinkAccessor
{
    private final ApplicationLinkService applicationLinkService;

    @Autowired
    public ApplicationLinkAccessor(ApplicationLinkService applicationLinkService)
    {
        this.applicationLinkService = applicationLinkService;
    }

    public ApplicationLink getApplicationLink(String appkey) throws IllegalArgumentException
    {
        for (ApplicationLink link : applicationLinkService.getApplicationLinks(RemotePluginContainerApplicationType.class))
        {
            if (appkey.equals(link.getProperty(RemotePluginContainerModuleDescriptor.PLUGIN_KEY_PROPERTY)))
            {
                return link;
            }
        }
        return null;
    }
}
