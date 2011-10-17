package com.atlassian.labs.remoteapps.modules.applinks;

import com.atlassian.applinks.api.ApplicationId;
import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.spi.application.ApplicationIdUtil;
import com.atlassian.applinks.spi.link.MutatingApplicationLinkService;
import com.atlassian.labs.remoteapps.descriptor.RemoteAppModuleDescriptor;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.event.PluginEventListener;
import com.atlassian.plugin.event.PluginEventManager;
import com.atlassian.plugin.event.events.PluginEnabledEvent;
import com.atlassian.plugin.event.events.PluginModuleEnabledEvent;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.atlassian.labs.remoteapps.modules.applinks.ApplicationTypeModuleGenerator.getGeneratedApplicationTypeModuleKey;

/**
 *
 */
public class ApplinkCreator
{
    private final Plugin plugin;
    private final RemoteAppApplicationType applicationType;
    private final ServiceTracker tracker;
    private final PluginEventManager pluginEventManager;

    private static final Logger log = LoggerFactory.getLogger(ApplinkCreator.class);
    private volatile MutatingApplicationLinkService applicationLinkService;

    public ApplinkCreator(Plugin plugin, final BundleContext bundleContext, PluginEventManager pluginEventManager, RemoteAppApplicationType applicationType)
    {
        this.plugin = plugin;
        this.applicationType = applicationType;
        this.tracker = new ServiceTracker(bundleContext, MutatingApplicationLinkService.class.getName(), new ServiceTrackerCustomizer()
        {
            @Override
            public Object addingService(ServiceReference reference)
            {
                Object svc = bundleContext.getService(reference);
                applicationLinkService = (MutatingApplicationLinkService) svc;
                evaluate();
                return svc;
            }

            @Override
            public void modifiedService(ServiceReference reference, Object service)
            {
                evaluate();
            }

            @Override
            public void removedService(ServiceReference reference, Object service)
            {
                evaluate();
            }
        });
        this.pluginEventManager = pluginEventManager;
        tracker.open();
        pluginEventManager.register(this);
        evaluate();
    }
    
    @PluginEventListener
    public void onPluginModuleEnabledEvent(PluginModuleEnabledEvent event)
    {
        if (event.getModule().getPlugin() == plugin)
        {
            evaluate();
        }
    }

    private void evaluate()
    {
        if (applicationLinkService == null)
        {
            return;
        }

        
        final ModuleDescriptor<?> moduleDescriptor = plugin.getModuleDescriptor(getGeneratedApplicationTypeModuleKey(applicationType.getId().get()));
        if (moduleDescriptor != null)
        {
            ApplicationLink link = applicationLinkService.getPrimaryApplicationLink(applicationType.getClass());
            if (link == null)
            {
                log.info("Creating an application link for the remote app type " + applicationType.getId());
                final ApplicationId applicationId = ApplicationIdUtil.generate(applicationType.getDefaultDetails().getRpcUrl());
                applicationLinkService.addApplicationLink(applicationId, applicationType, applicationType.getDefaultDetails());

            }
            else
            {
                log.info("Applink of type {} already exists", applicationType.getId());
            }
            destroy();
        }
    }

    public void destroy()
    {
        tracker.close();
        pluginEventManager.unregister(this);
    }
}
