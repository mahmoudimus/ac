package com.atlassian.plugin.connect.spi;

import com.atlassian.applinks.api.event.ApplicationLinkAddedEvent;
import com.atlassian.applinks.api.event.ApplicationLinkDeletedEvent;
import com.atlassian.event.api.EventListener;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.google.common.base.Supplier;
import org.springframework.beans.factory.DisposableBean;

import java.net.URI;

public interface DefaultRemotablePluginAccessorFactory extends RemotablePluginAccessorFactory, DisposableBean
{
    @Deprecated
    RemotablePluginAccessor create(ConnectAddonBean addon, Supplier<URI> displayUrl);

    @Deprecated
    RemotablePluginAccessor create(Plugin plugin, String pluginKey, Supplier<URI> displayUrl);

    void onApplicationLinkCreated(ApplicationLinkAddedEvent event);

    /**
     * Clear accessor if a application link is deleted
     *
     * @param event the event fired
     */
    @EventListener
    void onApplicationLinkRemoved(ApplicationLinkDeletedEvent event);
}
