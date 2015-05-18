package com.atlassian.plugin.connect.plugin.applinks;

import com.atlassian.applinks.spi.application.TypeId;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.spi.applinks.RemotePluginContainerApplicationType;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.atlassian.plugin.webresource.UrlMode;
import com.atlassian.plugin.webresource.WebResourceManager;

import java.net.URI;
import java.net.URISyntaxException;

/**
 *
 */
public class RemotePluginContainerApplicationTypeImpl implements RemotePluginContainerApplicationType
{
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
        return "connect.applinks.container.name";
    }

    public final URI getIconUrl()
    {
        try
        {
            return new URI(webResourceManager.getStaticPluginResource(plugin.getKey() + ":images", "images", UrlMode.ABSOLUTE) + "/atlassian-icon-16.png");
        }
        catch (URISyntaxException e)
        {
            throw new IllegalStateException("Missing icon url for plugin container type");
        }
    }
}
