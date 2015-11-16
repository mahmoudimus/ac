package it.com.atlassian.plugin.connect.testlifecycle.util;

import com.atlassian.plugin.Plugin;
import com.atlassian.upm.api.util.Option;
import com.atlassian.upm.spi.PluginControlHandler;
import com.atlassian.upm.spi.PluginInstallHandler;
import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.springframework.beans.factory.DisposableBean;

import java.io.File;
import java.io.IOException;

import static com.google.common.base.Preconditions.checkNotNull;

public class LifecycleUpmHelper implements DisposableBean
{

    private static final String DESCRIPTOR_PREFIX = "connect-descriptor-";

    private final BundleContext bundleContext;
    private ServiceTracker installServiceTracker;
    private ServiceTracker controlServiceTracker;

    public LifecycleUpmHelper(BundleContext bundleContext)
    {
        this.bundleContext = bundleContext;
    }

    public Plugin installAddon(String jsonDescriptor) throws IOException
    {
        File descriptor = createTempDescriptor(jsonDescriptor);
        return getUpmInstallHandler().installPlugin(descriptor, Option.some("application/json")).getPlugin();
    }

    private File createTempDescriptor(String json) throws IOException
    {
        File tmpFile = File.createTempFile(DESCRIPTOR_PREFIX, ".json");
        Files.write(json, tmpFile, Charsets.UTF_8);

        return tmpFile;
    }

    public PluginInstallHandler getUpmInstallHandler()
    {
        /*
        NOTE: we have to get the handler via OSGi by it's string name because we can't depend on the connect plugin.
         */
        if (null == installServiceTracker)
        {
            installServiceTracker = getServiceTracker(PluginInstallHandler.class);
        }

        checkNotNull(installServiceTracker);
        for (ServiceReference ref : installServiceTracker.getServiceReferences())
        {
            Object service = installServiceTracker.getService(ref);
            if (service.getClass().getName().contains("ConnectUPMInstallHandler"))
            {
                return (PluginInstallHandler) service;
            }
        }

        throw new IllegalStateException("Could not locate UPM install handler");
    }

    public PluginControlHandler getUpmControlHandler()
    {
        /*
        NOTE: we have to get the handler via OSGi by it's string name because we can't depend on the connect plugin.
         */
        if (null == controlServiceTracker)
        {
            controlServiceTracker = getServiceTracker(PluginControlHandler.class);
        }

        checkNotNull(controlServiceTracker);
        ServiceReference[] serviceReferences = controlServiceTracker.getServiceReferences();

        if (null != serviceReferences)
        {
            for (ServiceReference ref : serviceReferences)
            {
                Object service = controlServiceTracker.getService(ref);
                if (service.getClass().getName().endsWith(".ConnectUPMControlHandler"))
                {
                    return (PluginControlHandler) service;
                }
            }
        }

        throw new IllegalStateException("Could not locate UPM control handler");
    }

    private ServiceTracker getServiceTracker(Class clazz)
    {
        ServiceTracker tracker;

        synchronized (this)
        {
            tracker = new ServiceTracker(bundleContext, clazz.getName(), null);
            tracker.open();
        }

        return tracker;
    }

    @Override
    public void destroy() throws Exception
    {
        if (null != controlServiceTracker)
        {
            controlServiceTracker.close();
        }

        if (null != installServiceTracker)
        {
            installServiceTracker.close();
        }
    }
}
