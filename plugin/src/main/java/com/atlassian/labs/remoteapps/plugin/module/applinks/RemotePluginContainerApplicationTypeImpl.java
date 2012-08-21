package com.atlassian.labs.remoteapps.plugin.module.applinks;

import com.atlassian.applinks.spi.application.TypeId;
import com.atlassian.labs.remoteapps.spi.applinks.RemotePluginContainerApplicationType;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.atlassian.plugin.webresource.UrlMode;
import com.atlassian.plugin.webresource.WebResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;

/**
 *
 */
public class RemotePluginContainerApplicationTypeImpl implements RemotePluginContainerApplicationType
{
    private final Logger LOG = LoggerFactory.getLogger(this.getClass());

    private final WebResourceManager webResourceManager;
    private final Plugin plugin;

    public static final TypeId TYPE_ID = new TypeId("remote.plugin.container");

    public RemotePluginContainerApplicationTypeImpl(PluginRetrievalService pluginRetrievalService,
                                                    WebResourceManager webResourceManager
    )
    {
        this.plugin = pluginRetrievalService.getPlugin();
        this.webResourceManager = webResourceManager;
    }

    public TypeId getId()
    {
        return TYPE_ID;
    }

    public String getI18nKey()
    {
        return "plugin.container.name";
    }

    public final URI getIconUrl()
    {
        try
        {
            // todo: get a real icon
            return new URI(webResourceManager.getStaticPluginResource(plugin.getKey() +
                    ":images", "images", UrlMode.ABSOLUTE) + "/ajax-loader.gif");
        }
        catch (URISyntaxException e)
        {
            throw new IllegalStateException("Missing icon url for plugin container type");
        }
    }
}
