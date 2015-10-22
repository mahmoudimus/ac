package com.atlassian.plugin.connect.spi;

import com.atlassian.applinks.api.event.ApplicationLinkAddedEvent;
import com.atlassian.applinks.api.event.ApplicationLinkDeletedEvent;
import com.atlassian.event.api.EventListener;
import org.springframework.beans.factory.DisposableBean;

public interface DefaultRemotablePluginAccessorFactory extends RemotablePluginAccessorFactory, DisposableBean
{

    void onApplicationLinkCreated(ApplicationLinkAddedEvent event);

    /**
     * Clear accessor if a application link is deleted
     *
     * @param event the event fired
     */
    @EventListener
    void onApplicationLinkRemoved(ApplicationLinkDeletedEvent event);
}
