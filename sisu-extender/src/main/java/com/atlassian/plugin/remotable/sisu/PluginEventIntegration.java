package com.atlassian.plugin.remotable.sisu;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.event.PluginEventManager;
import com.atlassian.plugin.event.events.PluginContainerRefreshedEvent;
import com.google.inject.Injector;
import org.eclipse.sisu.EagerSingleton;
import org.osgi.framework.BundleContext;

import javax.inject.Inject;

/**
* TODO: Document this class / interface here
*
* @since v5.2
*/
@EagerSingleton
public class PluginEventIntegration
{
    @Inject
    public PluginEventIntegration(BundleContext bundleContext, final Plugin plugin, final Injector injector)
    {
        PluginEventManager pluginEventManager = (PluginEventManager) bundleContext.getService(bundleContext.getServiceReference(
                PluginEventManager.class.getName()));

        pluginEventManager.broadcast(new PluginContainerRefreshedEvent(new GuiceContainerAccessor(injector), plugin.getKey()));
    }
}
