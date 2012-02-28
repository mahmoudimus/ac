package com.atlassian.labs.remoteapps.integration.speakeasy;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.labs.speakeasy.external.SpeakeasyBackendService;
import com.atlassian.plugin.PluginAccessor;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Loads the speakeasy event listener when available
 */
@Component
public class SpeakeasyLoader implements DisposableBean
{
    private final ServiceTracker speakeasyBackendTracker;
    private volatile Object eventListener;

    @Autowired
    public SpeakeasyLoader(final EventPublisher eventPublisher, final BundleContext bundleContext,
            final PluginAccessor pluginAccessor)
    {
        speakeasyBackendTracker = new ServiceTracker(
                bundleContext,
                "com.atlassian.labs.speakeasy.external.SpeakeasyBackendService",
                new ServiceTrackerCustomizer()
                {
                    @Override
                    public Object addingService(ServiceReference reference)
                    {
                        SpeakeasyBackendService service = (SpeakeasyBackendService) bundleContext
                                .getService(reference);
                        eventListener = new SpeakeasyEventListener(
                                bundleContext,
                                pluginAccessor,
                                service);
                        eventPublisher.register(eventListener);
                        return service;
                    }

                    @Override
                    public void modifiedService(ServiceReference reference, Object service)
                    {
                        removedService(reference, service);
                        addingService(reference);
                    }

                    @Override
                    public void removedService(ServiceReference reference, Object service)
                    {
                        eventPublisher.unregister(eventListener);
                    }
                });
        speakeasyBackendTracker.open();

    }

    @Override
    public void destroy() throws Exception
    {
        speakeasyBackendTracker.close();
    }
}
